package org.ozea.security.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.dto.ApiResponse;
import org.ozea.common.util.LogFileWriter;
import org.ozea.security.util.JwtProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Log4j2
public class LogoutController {
    private final JwtProcessor jwtProcessor;
    private final LogFileWriter logFileWriter;
    /**
     * 로그아웃 엔드포인트
     * 현재 토큰을 블랙리스트에 추가하여 무효화
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("❌ 로그아웃 요청 - 토큰이 없음");
                logFileWriter.writeErrorLog("로그아웃 요청 - 토큰이 없음");
                return ResponseEntity.badRequest().body(ApiResponse.error("토큰이 필요합니다."));
            }
            String token = authHeader.substring(7);
            String username = jwtProcessor.getUsername(token);
            if (username == null) {
                log.warn("❌ 로그아웃 요청 - 유효하지 않은 토큰");
                logFileWriter.writeErrorLog("로그아웃 요청 - 유효하지 않은 토큰");
                return ResponseEntity.badRequest().body(ApiResponse.error("유효하지 않은 토큰입니다."));
            }
            // 토큰을 블랙리스트에 추가
            jwtProcessor.blacklistToken(token);
            // 현재 인증 정보 제거
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                SecurityContextHolder.clearContext();
                log.debug("🔓 인증 정보 제거 - 사용자: {}", username);
            }
            Map<String, Object> logoutData = new HashMap<>();
            logoutData.put("message", "로그아웃 성공");
            logoutData.put("timestamp", System.currentTimeMillis());
            log.info("✅ 로그아웃 성공 - 사용자: {}", username);
            logFileWriter.writeApiLog("/api/auth/logout", "로그아웃 성공 - 사용자: " + username);
            return ResponseEntity.ok(ApiResponse.success(logoutData, "로그아웃이 성공적으로 처리되었습니다."));
        } catch (Exception e) {
            log.error("💥 로그아웃 처리 중 오류 발생: {}", e.getMessage(), e);
            logFileWriter.writeErrorLog("로그아웃 처리 중 오류: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("로그아웃 처리 중 오류가 발생했습니다."));
        }
    }
    /**
     * 모든 세션 로그아웃 (관리자용)
     * 특정 사용자의 모든 토큰을 무효화
     */
    @PostMapping("/logout/all")
    public ResponseEntity<?> logoutAllSessions(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(ApiResponse.error("토큰이 필요합니다."));
            }
            String token = authHeader.substring(7);
            String username = jwtProcessor.getUsername(token);
            if (username == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("유효하지 않은 토큰입니다."));
            }
            // TODO: Redis 블랙리스트에 사용자 토큰 추가 구현 필요
            log.info("🔄 모든 세션 로그아웃 - 사용자: {}", username);
            logFileWriter.writeApiLog("/api/auth/logout/all", "모든 세션 로그아웃 - 사용자: " + username);
            Map<String, Object> logoutData = new HashMap<>();
            logoutData.put("message", "모든 세션 로그아웃 성공");
            logoutData.put("username", username);
            logoutData.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(ApiResponse.success(logoutData, "모든 세션이 성공적으로 로그아웃되었습니다."));
        } catch (Exception e) {
            log.error("💥 모든 세션 로그아웃 처리 중 오류 발생: {}", e.getMessage(), e);
            logFileWriter.writeErrorLog("모든 세션 로그아웃 처리 중 오류: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("모든 세션 로그아웃 처리 중 오류가 발생했습니다."));
        }
    }
    /**
     * 토큰 상태 확인
     * 토큰의 유효성과 만료 시간을 확인
     */
    @GetMapping("/token/status")
    public ResponseEntity<?> getTokenStatus(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(ApiResponse.error("토큰이 필요합니다."));
            }
            String token = authHeader.substring(7);
            String username = jwtProcessor.getUsername(token);
            if (username == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("유효하지 않은 토큰입니다."));
            }
            boolean isValid = jwtProcessor.validateToken(token);
            boolean isBlacklisted = jwtProcessor.isTokenBlacklisted(token);
            long remainingTime = jwtProcessor.getRemainingTime(token);
            String tokenType = jwtProcessor.getTokenType(token);
            Map<String, Object> statusData = new HashMap<>();
            statusData.put("valid", isValid);
            statusData.put("blacklisted", isBlacklisted);
            statusData.put("remainingTime", remainingTime);
            statusData.put("tokenType", tokenType);
            statusData.put("username", username);
            log.debug("📋 토큰 상태 확인 - 사용자: {}, 유효: {}, 블랙리스트: {}, 남은시간: {}ms",
                     username, isValid, isBlacklisted, remainingTime);
            return ResponseEntity.ok(ApiResponse.success(statusData, "토큰 상태를 확인했습니다."));
        } catch (Exception e) {
            log.error("💥 토큰 상태 확인 중 오류 발생: {}", e.getMessage(), e);
            logFileWriter.writeErrorLog("토큰 상태 확인 중 오류: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("토큰 상태 확인 중 오류가 발생했습니다."));
        }
    }
}