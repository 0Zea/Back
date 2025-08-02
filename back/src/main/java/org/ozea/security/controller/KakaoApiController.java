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

/**
 * 카카오 API를 처리하는 컨트롤러입니다.
 */
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
            log.error("카카오 인증 코드가 없습니다.");
            return ResponseEntity.badRequest().body("카카오 인증 코드가 필요합니다.");
        }
        
        try {
            // 1. 인증 코드로 액세스 토큰 요청
            String accessToken = getAccessToken(code);

            // 2. 액세스 토큰으로 사용자 정보 요청
            Map<String, Object> userInfo = getUserInfo(accessToken);

            // 3. 사용자 정보에서 이메일과 닉네임 추출
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
                // 이메일 정보가 없는 경우, 닉네임을 기반으로 임시 이메일을 생성합니다.
                // 이메일 동의를 하지 않은 사용자의 로그인을 허용하기 위한 처리입니다.
                email = UUID.randomUUID().toString() + "@noemail.kakao";
            }

            String nickname = (String) profile.get("nickname");
            if (nickname == null || nickname.isEmpty()) {
                throw new IllegalStateException("nickname is missing. profile: " + profile);
            }

            // 4. 기존 사용자 확인
            User existingUser = kakaoUserDetailsService.getUserByEmail(email);
            
            if (existingUser == null) {
                // 새로운 사용자인 경우, 임시 사용자 생성
                CustomUser customUser = (CustomUser) kakaoUserDetailsService.loadUserByUsername(email, nickname, accessToken, "");
                
                // 새로운 사용자에게도 임시 JWT 토큰 발급 (추가 정보 입력을 위해)
                String token = jwtProcessor.generateToken(email);
                
                // 새로운 사용자 정보를 포함한 응답
                UserInfoDTO userInfoDTO = UserInfoDTO.of(customUser.getUser());
                AuthResultDTO result = new AuthResultDTO(token, userInfoDTO, true);
                
                return ResponseEntity.ok(result);
            }

            // 5. 기존 사용자인 경우 로그인 처리
            CustomUser customUser = (CustomUser) kakaoUserDetailsService.loadUserByUsername(email, nickname, accessToken, "");

            // 6. JWT 토큰 생성
            String token = jwtProcessor.generateToken(email);

            // 7. 응답 데이터 생성
            UserInfoDTO userInfoDTO = UserInfoDTO.of(customUser.getUser());

            AuthResultDTO result = new AuthResultDTO(token, userInfoDTO, false);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("카카오 로그인 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 카카오 인증 코드로 액세스 토큰 요청
    private String getAccessToken(String code) throws Exception {
        // 실제 카카오 API 호출
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        // HTTP 클라이언트로 카카오 API 호출
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        // POST 요청 본문 구성
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
            // JSON 응답에서 access_token 추출
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = mapper.readValue(response.body(), Map.class);
            return (String) jsonMap.get("access_token");
        } else {
            throw new RuntimeException("카카오 토큰 요청 실패: " + response.statusCode() + " - " + response.body());
        }
    }

    // 액세스 토큰으로 사용자 정보 요청
    private Map<String, Object> getUserInfo(String accessToken) throws Exception {
        // 실제 카카오 API 호출
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        // HTTP 클라이언트로 카카오 API 호출
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(userInfoUrl))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        java.net.http.HttpResponse<String> response = client.send(request,
            java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // JSON 응답 파싱
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = mapper.readValue(response.body(), Map.class);
            return result;
        } else {
            throw new RuntimeException("카카오 사용자 정보 요청 실패: " + response.statusCode() + " - " + response.body());
        }
    }
} 