package org.ozea.api.allaccount.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ozea.api.allaccount.mapper.AllAccountMapper;
import org.ozea.api.allaccount.dto.request.AllAccountReqDto;
import org.ozea.api.util.CommonConstant;
import org.ozea.api.util.RSAUtil;
import org.ozea.api.util.RequestToken;
import org.ozea.asset.domain.BankAccountVO;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllAccountService {
    private final AllAccountMapper allAccountMapper;
    private String extractConnectedId(String jsonResponse) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonResponse);
            JSONObject dataObj = (JSONObject) json.get("data");
            if (dataObj != null) {
                return (String) dataObj.get("connectedId");
            }
        } catch (ParseException e) {
            log.error("ConnectedId 추출 중 오류 발생", e);
        }
        return null;
    }
    @Transactional
    public String getAllAccount(String userId) throws IOException,
            NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            InvalidKeySpecException, BadPaddingException, InvalidKeyException, InterruptedException, ParseException {
        AllAccountReqDto request = allAccountMapper.getUserAccountInfo(userId);
        if (request == null) {
            throw new RuntimeException("사용자 " + userId + "의 계정 정보를 찾을 수 없습니다.");
        }
        String result = callCodefApi(request);

        // DB에 계좌 정보 저장
        saveAccounts(userId, result);

        return result;
    }

    private void saveAccounts(String userId, String codefResponse) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(codefResponse);
            JSONObject data = (JSONObject) json.get("data");

            if (data != null && data.get("resDepositTrust") != null) {
                org.json.simple.JSONArray accounts = (org.json.simple.JSONArray) data.get("resDepositTrust");

                java.util.UUID userUUID = java.util.UUID.fromString(userId);

                for (Object accountObj : accounts) {
                    JSONObject account = (JSONObject) accountObj;
                    String accountNum = (String) account.get("resAccountDisplay");
                    java.math.BigInteger newBalance = new java.math.BigInteger((String) account.get("resAccountBalance"));

                    // UPSERT로 계좌 정보 처리
                    BankAccountVO accountVO = BankAccountVO.builder()
                            .userId(userUUID)
                            .bankName(extractBankName((String) account.get("resAccountName")))
                            .accountNum(accountNum)
                            .accountType((String) account.get("resAccountName"))
                            .balance(newBalance)
                            .build();

                    allAccountMapper.upsertBankAccount(accountVO);
                    log.info("계좌 처리: {} ({}원)", accountNum, account.get("resAccountBalance"));
                }

                log.info("계좌 처리 완료: {}개", accounts.size());
            }
        } catch (Exception e) {
            log.error("계좌 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("계좌 정보 저장 실패", e);
        }
    }

    private String extractBankName(String accountName) {
        if (accountName == null) return "기타은행";

        // 1금융권 은행명 추출
        if (accountName.contains("KB")) return "국민은행";
        if (accountName.contains("신한")) return "신한은행";
        if (accountName.contains("우리")) return "우리은행";
        if (accountName.contains("하나")) return "하나은행";
        if (accountName.contains("NH") || accountName.contains("농협")) return "농협은행";
        if (accountName.contains("IBK") || accountName.contains("기업")) return "기업은행";
        if (accountName.contains("SC제일")) return "SC제일은행";
        if (accountName.contains("씨티")) return "씨티은행";
        if (accountName.contains("KDB") || accountName.contains("산업")) return "산업은행";
        if (accountName.contains("수출입")) return "수출입은행";

        return "기타은행";
    }

    private String callCodefApi(AllAccountReqDto request) throws IOException,
            NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            InvalidKeySpecException, BadPaddingException, InvalidKeyException, InterruptedException, ParseException {
        String createUrl = "https://development.codef.io/v1/account/create";
        String token = RequestToken.getToken(CommonConstant.CLIENT_ID, CommonConstant.SECERET_KEY);

        if (token == null) {
            throw new RuntimeException("토큰 획득 실패");
        }

        String body = String.format("""
            {
              "accountList": [
                {
                  "countryCode": "%s",
                  "businessType": "%s",
                  "clientType": "%s",
                  "organization": "%s",
                  "loginType": "%s",
                  "id": "%s",
                  "password": "%s"
                }
              ]
            }
            """,
                request.getCountryCode(),
                request.getBusinessType(),
                request.getClientType(),
                request.getOrganization(),
                request.getLoginType(),
                request.getId(),
                RSAUtil.encryptRSA(request.getPassword(), CommonConstant.PUBLIC_KEY)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        String createResponseBody = restTemplate.exchange(createUrl, HttpMethod.POST, entity, String.class).getBody();
        if (createResponseBody == null) {
            throw new RuntimeException("계정 생성 응답이 null입니다");
        }

        String decodedCreateResponse = URLDecoder.decode(createResponseBody, StandardCharsets.UTF_8);
        String connectedId = extractConnectedId(decodedCreateResponse);
        if (connectedId == null) {
            throw new RuntimeException("connectedId 추출 실패");
        }

        String accountListUrl = "https://development.codef.io/v1/kr/bank/p/account/account-list";
        String accountListBody = String.format("""
            {
              "organization": "%s",
              "connectedId": "%s",
              "inquiryType": "0",
              "loginType": "%s"
            }
            """, request.getOrganization(), connectedId, request.getLoginType());

        HttpEntity<String> accountListEntity = new HttpEntity<>(accountListBody, headers);

        String accountListResponseBody = restTemplate.exchange(accountListUrl, HttpMethod.POST, accountListEntity, String.class).getBody();
        if (accountListResponseBody == null) {
            throw new RuntimeException("계좌 목록 응답이 null입니다");
        }

        return URLDecoder.decode(accountListResponseBody, StandardCharsets.UTF_8);
    }
}
