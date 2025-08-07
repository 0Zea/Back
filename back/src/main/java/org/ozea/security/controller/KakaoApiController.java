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
import org.ozea.common.dto.ApiResponse;
import org.ozea.common.exception.ErrorCode;
import org.ozea.common.util.LogFileWriter;
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
    @Autowired
    private LogFileWriter logFileWriter;
    @Value("${kakao.api.key}")
    private String kakaoApiKey;
    @Value("${kakao.redirect.uri}")
    private String kakaoRedirectUri;
    @GetMapping("/callback")
    public ResponseEntity<?> kakaoApiCallback(@RequestParam(value = "code", required = false) String code, HttpServletRequest request) {
        log.info("🔐 카카오 콜백 요청 - Code: {}", code != null ? code.substring(0, Math.min(code.length(), 10)) + "..." : "null");
        logFileWriter.writeKakaoLog("콜백 요청 - Code: " + (code != null ? code.substring(0, Math.min(code.length(), 10)) + "..." : "null"));
        if (code == null || code.isEmpty()) {
            log.error("❌ 카카오 인증 코드 누락");
            logFileWriter.writeErrorLog("카카오 인증 코드 누락");
            return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "카카오 인증 코드가 필요합니다."));
        }
        try {
            log.info("🔄 카카오 액세스 토큰 요청 중...");
            logFileWriter.writeKakaoLog("액세스 토큰 요청 중");
            String accessToken = getAccessToken(code);
            log.info("✅ 카카오 액세스 토큰 획득 성공");
            logFileWriter.writeKakaoLog("액세스 토큰 획득 성공");
            log.info("🔄 카카오 사용자 정보 요청 중...");
            logFileWriter.writeKakaoLog("사용자 정보 요청 중");
            Map<String, Object> userInfo = getUserInfo(accessToken);
            log.info("✅ 카카오 사용자 정보 획득 성공");
            logFileWriter.writeKakaoLog("사용자 정보 획득 성공");
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
            if (kakaoAccount == null) {
                log.error("❌ kakao_account 정보 누락 - userInfo: {}", userInfo);
                logFileWriter.writeErrorLog("kakao_account 정보 누락");
                return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "카카오 계정 정보를 가져올 수 없습니다."));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile == null) {
                log.error("❌ profile 정보 누락 - kakaoAccount: {}", kakaoAccount);
                logFileWriter.writeErrorLog("profile 정보 누락");
                return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "카카오 프로필 정보를 가져올 수 없습니다."));
            }
            String email = (String) kakaoAccount.get("email");
            if (email == null || email.isEmpty()) {
                email = UUID.randomUUID().toString() + "@noemail.kakao";
                log.warn("⚠️ 이메일 정보 없음, 임시 이메일 생성: {}", email);
                logFileWriter.writeKakaoLog("임시 이메일 생성: " + email);
            }
            String nickname = (String) profile.get("nickname");
            if (nickname == null || nickname.isEmpty()) {
                log.error("❌ nickname 정보 누락 - profile: {}", profile);
                logFileWriter.writeErrorLog("nickname 정보 누락");
                return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "카카오 닉네임 정보를 가져올 수 없습니다."));
            }
            log.info("👤 사용자 정보 - Email: {}, Nickname: {}", email, nickname);
            logFileWriter.writeKakaoLog("사용자 정보 - Email: " + email + ", Nickname: " + nickname);
            User existingUser = kakaoUserDetailsService.getUserByEmail(email);
            if (existingUser == null) {
                log.info("🆕 신규 사용자 등록 - Email: {}", email);
                logFileWriter.writeKakaoLog("신규 사용자 등록 - Email: " + email);
                CustomUser customUser = (CustomUser) kakaoUserDetailsService.loadUserByUsername(email, nickname, accessToken, "");
                // 신규 사용자를 DB에 등록
                kakaoUserDetailsService.registerNewUser(customUser.getUser());
                String token = jwtProcessor.generateAccessToken(email);
                String refreshToken = jwtProcessor.generateRefreshToken(email);
                UserInfoDTO userInfoDTO = UserInfoDTO.of(customUser.getUser());
                AuthResultDTO result = new AuthResultDTO(token, refreshToken, userInfoDTO, true);
                log.info("✅ 신규 사용자 로그인 성공 - Email: {}", email);
                logFileWriter.writeKakaoLog("신규 사용자 로그인 성공 - Email: " + email);
                return ResponseEntity.ok(ApiResponse.success(result, "카카오 로그인 성공"));
            }
            log.info("👤 기존 사용자 로그인 - Email: {}", email);
            logFileWriter.writeKakaoLog("기존 사용자 로그인 - Email: " + email);
            CustomUser customUser = (CustomUser) kakaoUserDetailsService.loadUserByUsername(email, nickname, accessToken, "");
            String token = jwtProcessor.generateAccessToken(email);
            String refreshToken = jwtProcessor.generateRefreshToken(email);
            UserInfoDTO userInfoDTO = UserInfoDTO.of(customUser.getUser());
            AuthResultDTO result = new AuthResultDTO(token, refreshToken, userInfoDTO, false);
            log.info("✅ 기존 사용자 로그인 성공 - Email: {}", email);
            logFileWriter.writeKakaoLog("기존 사용자 로그인 성공 - Email: " + email);
            return ResponseEntity.ok(ApiResponse.success(result, "카카오 로그인 성공"));
        } catch (Exception e) {
            log.error("💥 카카오 로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            logFileWriter.writeErrorLog("카카오 로그인 처리 중 오류: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.EXTERNAL_API_ERROR, "카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    /**
     * 테스트용 카카오 콜백 엔드포인트
     * 실제 카카오 API 키가 없을 때 사용
     */
    @GetMapping("/callback/test")
    public ResponseEntity<?> kakaoApiCallbackTest(@RequestParam(value = "code", required = false) String code, HttpServletRequest request) {
        log.info("🧪 테스트용 카카오 콜백 요청 - Code: {}", code != null ? code.substring(0, Math.min(code.length(), 10)) + "..." : "null");
        if (code == null || code.isEmpty()) {
            log.error("❌ 카카오 인증 코드 누락");
            return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "카카오 인증 코드가 필요합니다."));
        }
        try {
            // 테스트용 사용자 정보 생성
            String email = "test@kakao.com";
            String nickname = "테스트 사용자";
            String accessToken = "test_access_token_" + System.currentTimeMillis();
            log.info("👤 테스트 사용자 정보 - Email: {}, Nickname: {}", email, nickname);
            User existingUser = kakaoUserDetailsService.getUserByEmail(email);
            if (existingUser == null) {
                log.info("🆕 테스트 신규 사용자 등록 - Email: {}", email);
                CustomUser customUser = (CustomUser) kakaoUserDetailsService.loadUserByUsername(email, nickname, accessToken, "");
                // 신규 사용자를 DB에 등록
                kakaoUserDetailsService.registerNewUser(customUser.getUser());
                String token = jwtProcessor.generateAccessToken(email);
                String refreshToken = jwtProcessor.generateRefreshToken(email);
                UserInfoDTO userInfoDTO = UserInfoDTO.of(customUser.getUser());
                AuthResultDTO result = new AuthResultDTO(token, refreshToken, userInfoDTO, true);
                log.info("✅ 테스트 신규 사용자 로그인 성공 - Email: {}", email);
                return ResponseEntity.ok(ApiResponse.success(result, "테스트 카카오 로그인 성공"));
            }
            log.info("👤 테스트 기존 사용자 로그인 - Email: {}", email);
            CustomUser customUser = (CustomUser) kakaoUserDetailsService.loadUserByUsername(email, nickname, accessToken, "");
            String token = jwtProcessor.generateAccessToken(email);
            String refreshToken = jwtProcessor.generateRefreshToken(email);
            UserInfoDTO userInfoDTO = UserInfoDTO.of(customUser.getUser());
            AuthResultDTO result = new AuthResultDTO(token, refreshToken, userInfoDTO, false);
            log.info("✅ 테스트 기존 사용자 로그인 성공 - Email: {}", email);
            return ResponseEntity.ok(ApiResponse.success(result, "테스트 카카오 로그인 성공"));
        } catch (Exception e) {
            log.error("💥 테스트 카카오 로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.EXTERNAL_API_ERROR, "테스트 카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage()));
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
        log.debug("🔄 카카오 토큰 요청 - URL: {}, Body: {}", tokenUrl, requestBody.replace(kakaoApiKey, "***"));
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(tokenUrl))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
        java.net.http.HttpResponse<String> response = client.send(request,
            java.net.http.HttpResponse.BodyHandlers.ofString());
        log.debug("📡 카카오 토큰 응답 - Status: {}, Body: {}", response.statusCode(), response.body());
        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = mapper.readValue(response.body(), Map.class);
            return (String) jsonMap.get("access_token");
        } else {
            log.error("❌ 카카오 토큰 요청 실패 - Status: {}, Response: {}", response.statusCode(), response.body());
            throw new RuntimeException("카카오 토큰 요청 실패: " + response.statusCode() + " - " + response.body());
        }
    }
    private Map<String, Object> getUserInfo(String accessToken) throws Exception {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        log.debug("🔄 카카오 사용자 정보 요청 - URL: {}", userInfoUrl);
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(userInfoUrl))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();
        java.net.http.HttpResponse<String> response = client.send(request,
            java.net.http.HttpResponse.BodyHandlers.ofString());
        log.debug("📡 카카오 사용자 정보 응답 - Status: {}, Body: {}", response.statusCode(), response.body());
        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = mapper.readValue(response.body(), Map.class);
            return result;
        } else {
            log.error("❌ 카카오 사용자 정보 요청 실패 - Status: {}, Response: {}", response.statusCode(), response.body());
            throw new RuntimeException("카카오 사용자 정보 요청 실패: " + response.statusCode() + " - " + response.body());
        }
    }
}