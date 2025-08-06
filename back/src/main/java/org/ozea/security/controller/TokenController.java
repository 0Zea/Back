package org.ozea.security.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.dto.ApiResponse;
import org.ozea.common.exception.ErrorCode;
import org.ozea.common.util.LogFileWriter;
import org.ozea.security.util.JwtProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/token")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Log4j2
public class TokenController {

    private final JwtProcessor jwtProcessor;
    private final LogFileWriter logFileWriter;

    /**
     * 토큰 갱신 엔드포인트
     * Refresh Token을 사용하여 새로운 Access Token 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            // Authorization 헤더에서 Refresh Token 추출
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("❌ Refresh Token이 제공되지 않음");
                logFileWriter.writeErrorLog("Refresh Token이 제공되지 않음");
                return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "Refresh Token이 필요합니다."));
            }

            String refreshToken = authHeader.substring(7);
            log.info("🔄 토큰 갱신 요청 - Refresh Token: {}", refreshToken.substring(0, Math.min(refreshToken.length(), 20)) + "...");
            logFileWriter.writeApiLog("/api/auth/token/refresh", "토큰 갱신 요청");

            // Refresh Token 검증
            if (!jwtProcessor.validateToken(refreshToken)) {
                log.warn("❌ 유효하지 않은 Refresh Token");
                logFileWriter.writeErrorLog("유효하지 않은 Refresh Token");
                return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INVALID_TOKEN, "유효하지 않은 Refresh Token입니다."));
            }

            // 토큰 타입 확인
            String tokenType = jwtProcessor.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                log.warn("❌ 잘못된 토큰 타입: {}", tokenType);
                logFileWriter.writeErrorLog("잘못된 토큰 타입: " + tokenType);
                return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INVALID_TOKEN, "Refresh Token이 아닙니다."));
            }

            // 사용자명 추출
            String username = jwtProcessor.getUsername(refreshToken);
            if (username == null) {
                log.warn("❌ 토큰에서 사용자명 추출 실패");
                logFileWriter.writeErrorLog("토큰에서 사용자명 추출 실패");
                return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INVALID_TOKEN, "토큰에서 사용자 정보를 추출할 수 없습니다."));
            }

            // 새로운 Access Token 생성
            String newAccessToken = jwtProcessor.generateAccessToken(username);
            String newRefreshToken = jwtProcessor.generateRefreshToken(username);

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("accessToken", newAccessToken);
            tokenData.put("refreshToken", newRefreshToken);
            tokenData.put("tokenType", "Bearer");
            tokenData.put("expiresIn", 3600); // 1시간

            log.info("✅ 토큰 갱신 성공 - 사용자: {}", username);
            logFileWriter.writeApiLog("/api/auth/token/refresh", "토큰 갱신 성공 - 사용자: " + username);

            return ResponseEntity.ok(ApiResponse.success(tokenData, "토큰이 성공적으로 갱신되었습니다."));

        } catch (Exception e) {
            log.error("💥 토큰 갱신 중 오류 발생: {}", e.getMessage(), e);
            logFileWriter.writeErrorLog("토큰 갱신 중 오류: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 갱신 중 오류가 발생했습니다."));
        }
    }

    /**
     * 토큰 검증 엔드포인트
     * Access Token의 유효성을 확인
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "Access Token이 필요합니다."));
            }

            String accessToken = authHeader.substring(7);
            log.info("🔍 토큰 검증 요청");
            logFileWriter.writeApiLog("/api/auth/token/validate", "토큰 검증 요청");

            if (!jwtProcessor.validateToken(accessToken)) {
                log.warn("❌ 유효하지 않은 Access Token");
                logFileWriter.writeErrorLog("유효하지 않은 Access Token");
                return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INVALID_TOKEN, "유효하지 않은 Access Token입니다."));
            }

            String tokenType = jwtProcessor.getTokenType(accessToken);
            if (!"access".equals(tokenType)) {
                log.warn("❌ 잘못된 토큰 타입: {}", tokenType);
                logFileWriter.writeErrorLog("잘못된 토큰 타입: " + tokenType);
                return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INVALID_TOKEN, "Access Token이 아닙니다."));
            }

            String username = jwtProcessor.getUsername(accessToken);
            
            Map<String, Object> validationData = new HashMap<>();
            validationData.put("valid", true);
            validationData.put("username", username);
            validationData.put("tokenType", tokenType);

            log.info("✅ 토큰 검증 성공 - 사용자: {}", username);
            logFileWriter.writeApiLog("/api/auth/token/validate", "토큰 검증 성공 - 사용자: " + username);

            return ResponseEntity.ok(ApiResponse.success(validationData, "토큰이 유효합니다."));

        } catch (Exception e) {
            log.error("💥 토큰 검증 중 오류 발생: {}", e.getMessage(), e);
            logFileWriter.writeErrorLog("토큰 검증 중 오류: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 검증 중 오류가 발생했습니다."));
        }
    }

    /**
     * 토큰 정보 조회 엔드포인트
     * 토큰의 만료 시간 등 정보를 반환
     */
    @GetMapping("/info")
    public ResponseEntity<?> getTokenInfo(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "토큰이 필요합니다."));
            }

            String token = authHeader.substring(7);
            log.info("📋 토큰 정보 조회 요청");
            logFileWriter.writeApiLog("/api/auth/token/info", "토큰 정보 조회 요청");

            if (!jwtProcessor.validateToken(token)) {
                return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다."));
            }

            String username = jwtProcessor.getUsername(token);
            String tokenType = jwtProcessor.getTokenType(token);

            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("username", username);
            tokenInfo.put("tokenType", tokenType);
            tokenInfo.put("valid", true);

            log.info("✅ 토큰 정보 조회 성공 - 사용자: {}, 타입: {}", username, tokenType);
            logFileWriter.writeApiLog("/api/auth/token/info", "토큰 정보 조회 성공 - 사용자: " + username);

            return ResponseEntity.ok(ApiResponse.success(tokenInfo, "토큰 정보를 조회했습니다."));

        } catch (Exception e) {
            log.error("💥 토큰 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            logFileWriter.writeErrorLog("토큰 정보 조회 중 오류: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 정보 조회 중 오류가 발생했습니다."));
        }
    }
} 