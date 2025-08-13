package org.ozea.api.allaccount.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ozea.api.allaccount.dto.request.AllAccountReqDto;
import org.ozea.api.allaccount.mapper.AllAccountMapper;
import org.ozea.api.util.CommonConstant;
import org.ozea.api.util.RSAUtil;
import org.ozea.api.util.RequestToken;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllAccountService {

    private final AllAccountMapper allAccountMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String BASE_URL = "https://development.codef.io";
    private static final MediaType JSON_UTF8 = new MediaType("application", "json", StandardCharsets.UTF_8);

    public String createAccountFromDB() throws Exception {
        AllAccountReqDto request = allAccountMapper.getAccountInfo();
        if (request == null) {
            throw new IllegalStateException("DB에서 계정 정보를 찾을 수 없습니다.");
        }
        String token = RequestToken.getToken(CommonConstant.CLIENT_ID, CommonConstant.SECERET_KEY);

        // 1) account/create
        String createBody = toCreateBody(request);
        String createResponse = postJson(
                BASE_URL + "/v1/account/create",
                token,
                createBody
        );
        String connectedId = extractConnectedId(createResponse);
        if (connectedId == null || connectedId.isBlank()) {
            throw new IllegalStateException("connectedId 추출 실패");
        }

        String listBody = """
            {
              "organization": "%s",
              "connectedId": "%s"
            }
            """.formatted(request.getOrganization(), connectedId);

        return postJson(BASE_URL + "/v1/kr/bank/p/account/list", token, listBody);
    }

    public String getYeomsky95Assets() throws Exception {
        AllAccountReqDto request = allAccountMapper.getYeomsky95AccountInfo();
        if (request == null) {
            throw new IllegalStateException("yeomsky95 계정 정보를 찾을 수 없습니다.");
        }
        String token = RequestToken.getToken(CommonConstant.CLIENT_ID, CommonConstant.SECERET_KEY);

        String createBody = toCreateBody(request);
        String createResponse = postJson(BASE_URL + "/v1/account/create", token, createBody);

        String connectedId = extractConnectedId(createResponse);
        if (connectedId == null || connectedId.isBlank()) {
            throw new IllegalStateException("connectedId 추출 실패");
        }

        String listBody = """
            {
              "organization": "%s",
              "connectedId": "%s"
            }
            """.formatted(request.getOrganization(), connectedId);

        return postJson(BASE_URL + "/v1/kr/bank/p/account/list", token, listBody);
    }

    private String toCreateBody(AllAccountReqDto req)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        String encPw = RSAUtil.encryptRSA(req.getPassword(), CommonConstant.PUBLIC_KEY);
        return """
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
            """.formatted(
                nullToEmpty(req.getCountryCode()),
                nullToEmpty(req.getBusinessType()),
                nullToEmpty(req.getClientType()),
                nullToEmpty(req.getOrganization()),
                nullToEmpty(req.getLoginType()),
                nullToEmpty(req.getId()),
                encPw
        );
    }

    private String postJson(String url, String bearerToken, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(JSON_UTF8);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                log.warn("CODEF API non-2xx: url={}, status={}, body={}", url, resp.getStatusCode(), resp.getBody());
                throw new IllegalStateException("외부 API 응답 오류: " + resp.getStatusCode());
            }
            return resp.getBody();
        } catch (RestClientException e) {
            log.error("CODEF API 호출 실패: url={}, err={}", url, e.getMessage(), e);
            throw e;
        }
    }

    private String extractConnectedId(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.path("data");
            if (data.isMissingNode()) return null;
            JsonNode id = data.path("connectedId");
            return id.isMissingNode() || id.isNull() ? null : id.asText();
        } catch (Exception e) {
            log.warn("connectedId 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}