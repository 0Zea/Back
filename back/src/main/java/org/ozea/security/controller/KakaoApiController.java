package org.ozea.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ozea.common.dto.ApiResponse;
import org.ozea.common.exception.ErrorCode;
import org.ozea.common.util.LogFileWriter;
import org.ozea.security.account.domain.CustomUser;
import org.ozea.security.account.dto.AuthResultDTO;
import org.ozea.security.account.dto.UserInfoDTO;
import org.ozea.security.config.KakaoUserDetailsService;
import org.ozea.security.util.JwtProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/kakao")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KakaoApiController {

    private static final Logger log = LoggerFactory.getLogger(KakaoApiController.class);

    private final ObjectMapper objectMapper;
    private final KakaoUserDetailsService kakaoUserDetailsService;
    private final JwtProcessor jwtProcessor;
    private final LogFileWriter logFileWriter;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.redirect.uri}")
    private String kakaoRedirectUri;

    @GetMapping(value = "/callback", produces = "application/json")
    public ResponseEntity<?> kakaoApiCallback(@RequestParam("code") String code) {
        final String codePreview = code != null ? code.substring(0, Math.min(code.length(), 10)) + "..." : "null";
        log.info("🔐 카카오 콜백 요청 - Code: {}", codePreview);

        safeKakaoLog("콜백 요청 - Code: " + codePreview);

        if (kakaoApiKey == null || kakaoApiKey.isBlank()) {
            return serverError("KAKAO_API_KEY 누락");
        }
        if (kakaoRedirectUri == null || kakaoRedirectUri.isBlank()) {
            return serverError("KAKAO_REDIRECT_URI 누락");
        }
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "카카오 인증 코드가 필요합니다."));
        }

        try {
            String accessToken = getAccessToken(code);
            Map<String, Object> userInfo = getUserInfo(accessToken);

            Map<String, Object> kakaoAccount = safeMap(userInfo.get("kakao_account"));
            if (kakaoAccount == null) {
                return badRequest("kakao_account 누락(동의 범위 확인 필요: account_email 등)");
            }
            Map<String, Object> profile = safeMap(kakaoAccount.get("profile"));
            if (profile == null) {
                return badRequest("profile 누락(카카오 프로필 동의 필요)");
            }

            String email = asString(kakaoAccount.get("email"));
            if (email == null || email.isBlank()) {
                email = UUID.randomUUID() + "@noemail.kakao";
                log.warn("⚠️ 이메일 정보 없음 → 임시 이메일 사용: {}", email);
                safeKakaoLog("임시 이메일 생성: " + email);
            }
            String nickname = asString(profile.get("nickname"));
            if (nickname == null || nickname.isBlank()) {
                return badRequest("nickname 누락(프로필 공개/동의 확인)");
            }

            log.info("👤 카카오 사용자: email={}, nickname={}", email, nickname);

            boolean isNew;
            try {
                isNew = (kakaoUserDetailsService.getUserByEmail(email) == null);
            } catch (Exception e) {
                log.error("사용자 조회 실패: {}", e.getMessage(), e);
                return serverError("사용자 조회 중 오류가 발생했습니다.");
            }

            CustomUser customUser;
            try {
                customUser = (CustomUser) kakaoUserDetailsService.loadUserByUsername(email, nickname, accessToken, "");
                if (isNew) {
                    kakaoUserDetailsService.registerNewUser(customUser.getUser());
                }
            } catch (IllegalArgumentException ex) {
                log.warn("사용자 로드/등록 실패: {}", ex.getMessage());
                return badRequest("사용자 처리 실패: " + ex.getMessage());
            } catch (Exception ex) {
                log.error("사용자 로드/등록 중 예외: {}", ex.getMessage(), ex);
                return serverError("사용자 처리 중 오류가 발생했습니다.");
            }

            String token = jwtProcessor.generateAccessToken(email);
            String refreshToken = jwtProcessor.generateRefreshToken(email);

            UserInfoDTO userInfoDTO = UserInfoDTO.of(customUser.getUser());
            AuthResultDTO result = new AuthResultDTO(token, refreshToken, userInfoDTO, isNew);

            return ResponseEntity.ok(ApiResponse.success(result, "카카오 로그인 성공"));

        } catch (IllegalArgumentException e) {
            log.warn("Kakao client error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCode.EXTERNAL_API_ERROR, e.getMessage()));
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 서버 오류: {}", e.getMessage(), e);
            return serverError("서버 오류가 발생했습니다.");
        }
    }

    private String getAccessToken(String code) throws Exception {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        String body = "grant_type=authorization_code"
                + "&client_id="    + URLEncoder.encode(kakaoApiKey, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(kakaoRedirectUri, StandardCharsets.UTF_8)
                + "&code="         + URLEncoder.encode(code, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(8))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            Map<String, Object> json = objectMapper.readValue(resp.body(), Map.class);
            String accessToken = asString(json.get("access_token"));
            if (accessToken == null) throw new IllegalArgumentException("TOKEN_PARSE_ERROR: access_token 누락");
            return accessToken;
        }
        String reason = "TOKEN_EXCHANGE_" + resp.statusCode() + ": " + truncate(resp.body(), 400);
        log.warn("Kakao token exchange failed -> {}", reason);
        throw new IllegalArgumentException(reason);
    }

    private Map<String, Object> getUserInfo(String accessToken) throws Exception {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(userInfoUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            return objectMapper.readValue(resp.body(), Map.class);
        }
        String reason = "USERINFO_" + resp.statusCode() + ": " + truncate(resp.body(), 400);
        log.warn("Kakao userinfo failed -> {}", reason);
        throw new IllegalArgumentException(reason);
    }

    private void safeKakaoLog(String msg) {
        try { if (logFileWriter != null) logFileWriter.writeKakaoLog(msg); }
        catch (Exception ignore) { log.debug("logFileWriter 실패(무시): {}", ignore.toString()); }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeMap(Object o) {
        if (o instanceof Map) return (Map<String, Object>) o;
        return null;
    }

    private String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }

    private ResponseEntity<?> badRequest(String msg) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.EXTERNAL_API_ERROR, msg));
    }

    private ResponseEntity<?> serverError(String msg) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, msg));
    }
}
