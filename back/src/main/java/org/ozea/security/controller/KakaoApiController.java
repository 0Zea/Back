package org.ozea.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ozea.security.account.domain.CustomUser;
import org.ozea.security.config.KakaoUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.ozea.security.account.dto.AuthResultDTO;
import org.ozea.security.account.dto.UserInfoDTO;
import org.ozea.security.util.JwtProcessor;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.UUID;
import org.ozea.user.domain.User;

@RestController
@RequestMapping("/api/auth/kakao")
@CrossOrigin(origins = "*")
public class KakaoApiController {

    private static final Logger log = LoggerFactory.getLogger(KakaoApiController.class);

    @Autowired
    private KakaoUserDetailsService kakaoUserDetailsService;

    @Autowired
    private JwtProcessor jwtProcessor;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.redirect.uri}")
    private String kakaoRedirectUri;

    @GetMapping("/callback")
    public ResponseEntity<?> kakaoApiCallback(@RequestParam(value = "code", required = false) String code, HttpServletRequest request) {
        
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("카카오 인증 코드가 필요합니다.");
        }
        
        try {
            String accessToken = getAccessToken(code);

            Map<String, Object> userInfo = getUserInfo(accessToken);

            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
            if (kakaoAccount == null) {
                throw new IllegalStateException("kakao_account is null. userInfo: " + userInfo);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile == null) {
                throw new IllegalStateException("profile is null. kakaoAccount: " + kakaoAccount);
            }

            String email = (String) kakaoAccount.get("email");
            if (email == null || email.isEmpty()) {
                email = UUID.randomUUID().toString() + "@noemail.kakao";
            }

            String nickname = (String) profile.get("nickname");
            if (nickname == null || nickname.isEmpty()) {
                throw new IllegalStateException("nickname is missing. profile: " + profile);
            }

            User existingUser = kakaoUserDetailsService.getUserByEmail(email);
            
            if (existingUser == null) {
                CustomUser customUser = (CustomUser) kakaoUserDetailsService.loadUserByUsername(email, nickname, accessToken, "");
                
                String token = jwtProcessor.generateToken(email);
                
                UserInfoDTO userInfoDTO = UserInfoDTO.of(customUser.getUser());
                AuthResultDTO result = new AuthResultDTO(token, userInfoDTO, true);
                
                return ResponseEntity.ok(result);
            }

            CustomUser customUser = (CustomUser) kakaoUserDetailsService.loadUserByUsername(email, nickname, accessToken, "");

            String token = jwtProcessor.generateToken(email);

            UserInfoDTO userInfoDTO = UserInfoDTO.of(customUser.getUser());

            AuthResultDTO result = new AuthResultDTO(token, userInfoDTO, false);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String getAccessToken(String code) throws Exception {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        String requestBody = String.format(
            "grant_type=authorization_code&client_id=%s&redirect_uri=%s&code=%s",
            kakaoApiKey,
            kakaoRedirectUri,
            code
        );

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(tokenUrl))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        java.net.http.HttpResponse<String> response = client.send(request,
            java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = mapper.readValue(response.body(), Map.class);
            return (String) jsonMap.get("access_token");
        } else {
            throw new RuntimeException("카카오 토큰 요청 실패: " + response.statusCode() + " - " + response.body());
        }
    }

    private Map<String, Object> getUserInfo(String accessToken) throws Exception {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(userInfoUrl))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        java.net.http.HttpResponse<String> response = client.send(request,
            java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = mapper.readValue(response.body(), Map.class);
            return result;
        } else {
            throw new RuntimeException("카카오 사용자 정보 요청 실패: " + response.statusCode() + " - " + response.body());
        }
    }
} 