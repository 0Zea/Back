package org.ozea.api.account.service;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ozea.api.account.dto.response.AllAccountResDto;
import org.ozea.api.account.mapper.AllAccountMapper;
import org.ozea.api.account.dto.request.AllAccountReqDto;
import org.ozea.api.util.CommonConstant;
import org.ozea.api.util.RSAUtil;
import org.ozea.api.util.RequestToken;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllAccountService {
    private final AllAccountMapper allAccountMapper;

    public String createAccountFromDB() throws IOException,
            NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            InvalidKeySpecException, BadPaddingException, InvalidKeyException, InterruptedException, ParseException {

        // 1. DB에서 계정 정보 조회
        AllAccountReqDto request = allAccountMapper.getAccountInfo();
        if (request == null) {
            throw new RuntimeException("DB에서 계정 정보를 찾을 수 없습니다.");
        }

        // 2. CODEF 계정 등록 API URL
        String createUrl = "https://development.codef.io/v1/account/create";

        // 3. CODEF 토큰 발급
        String token = RequestToken.getToken(CommonConstant.CLIENT_ID, CommonConstant.SECERET_KEY);

        // 4. 계정 생성 요청 Body
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

        // 5. 요청 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // 6. 계정 생성 API 호출
        ResponseEntity<String> createResponse = restTemplate.exchange(createUrl, HttpMethod.POST, entity, String.class);

        String decodedCreateResponse = URLDecoder.decode(createResponse.getBody(), StandardCharsets.UTF_8);
        System.out.println("===== CODEF Create Account Response =====");
        System.out.println(decodedCreateResponse);

        // 7. connectedId 추출
        String connectedId = extractConnectedId(decodedCreateResponse);
        System.out.println("===== Extracted connectedId =====");
        System.out.println("'" + connectedId + "'");

        if (connectedId == null) {
            throw new RuntimeException("connectedId 추출 실패");
        }

        // 8. 계좌 조회 API 호출
        String accountListUrl = "https://development.codef.io/v1/kr/bank/p/account/list";

        String accountListBody = String.format("""
            {
              "organization": "%s",
              "connectedId": "%s"
            }
            """, request.getOrganization(), connectedId);

        System.out.println("===== Request Body to Account List API =====");
        System.out.println(accountListBody);

        HttpEntity<String> accountListEntity = new HttpEntity<>(accountListBody, headers);

        ResponseEntity<String> accountListResponse = restTemplate.exchange(accountListUrl, HttpMethod.POST, accountListEntity, String.class);

        String decodedAccountListResponse = URLDecoder.decode(accountListResponse.getBody(), StandardCharsets.UTF_8);
        System.out.println("===== CODEF Account List Response =====");
        System.out.println(decodedAccountListResponse);

        return decodedAccountListResponse;
    }


    /**
     * connectedId 추출 메서드
     */
    private String extractConnectedId(String jsonResponse) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonResponse);
            JSONObject dataObj = (JSONObject) json.get("data");
            if (dataObj != null) {
                return (String) dataObj.get("connectedId");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * yeomsky95 사용자의 자산 정보를 가져오는 메서드
     */
    public String getYeomsky95Assets() throws IOException,
            NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            InvalidKeySpecException, BadPaddingException, InvalidKeyException, InterruptedException, ParseException {

        // 1. DB에서 yeomsky95 계정 정보 조회
        AllAccountReqDto request = allAccountMapper.getYeomsky95AccountInfo();
        if (request == null) {
            throw new RuntimeException("yeomsky95 계정 정보를 찾을 수 없습니다.");
        }

        // 2. CODEF 계정 등록 API URL
        String createUrl = "https://development.codef.io/v1/account/create";

        // 3. CODEF 토큰 발급
        String token = RequestToken.getToken(CommonConstant.CLIENT_ID, CommonConstant.SECERET_KEY);

        // 4. 계정 생성 요청 Body
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

        // 5. 요청 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // 6. 계정 생성 API 호출
        ResponseEntity<String> createResponse = restTemplate.exchange(createUrl, HttpMethod.POST, entity, String.class);

        String decodedCreateResponse = URLDecoder.decode(createResponse.getBody(), StandardCharsets.UTF_8);
        System.out.println("===== CODEF Create Account Response for yeomsky95 =====");
        System.out.println(decodedCreateResponse);

        // 7. connectedId 추출
        String connectedId = extractConnectedId(decodedCreateResponse);
        System.out.println("===== Extracted connectedId for yeomsky95 =====");
        System.out.println("'" + connectedId + "'");

        if (connectedId == null) {
            throw new RuntimeException("connectedId 추출 실패");
        }

        // 8. 계좌 조회 API 호출
        String accountListUrl = "https://development.codef.io/v1/kr/bank/p/account/list";

        String accountListBody = String.format("""
            {
              "organization": "%s",
              "connectedId": "%s"
            }
            """, request.getOrganization(), connectedId);

        System.out.println("===== Request Body to Account List API for yeomsky95 =====");
        System.out.println(accountListBody);

        HttpEntity<String> accountListEntity = new HttpEntity<>(accountListBody, headers);

        ResponseEntity<String> accountListResponse = restTemplate.exchange(accountListUrl, HttpMethod.POST, accountListEntity, String.class);

        String decodedAccountListResponse = URLDecoder.decode(accountListResponse.getBody(), StandardCharsets.UTF_8);
        System.out.println("===== CODEF Account List Response for yeomsky95 =====");
        System.out.println(decodedAccountListResponse);

        return decodedAccountListResponse;
    }
}
