package org.ozea.security.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.security.account.dto.AuthResultDTO;
import org.ozea.security.account.dto.UserInfoDTO;
import org.ozea.security.util.JsonResponse;
import org.ozea.security.util.JwtProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthController {
    
    private final JwtProcessor jwtProcessor;
    
    // 토큰 갱신 API
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String refreshToken = request.getHeader("X-Refresh-Token");
            
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token이 필요합니다."));
            }
            
            // Refresh Token 검증
            if (!jwtProcessor.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "유효하지 않은 refresh token입니다."));
            }
            
            // 토큰 타입 확인
            String tokenType = jwtProcessor.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "잘못된 토큰 타입입니다."));
            }
            
            // 사용자명 추출
            String username = jwtProcessor.getUsername(refreshToken);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "토큰에서 사용자 정보를 추출할 수 없습니다."));
            }
            
            // 새로운 Access Token 생성
            String newAccessToken = jwtProcessor.generateAccessToken(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("message", "토큰이 성공적으로 갱신되었습니다.");
            
            log.info("토큰 갱신 성공: {}", username);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "토큰 갱신 중 오류가 발생했습니다."));
        }
    }
    
    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            // 세션 무효화
            request.getSession().invalidate();
            
            log.info("로그아웃 성공");
            return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
            
        } catch (Exception e) {
            log.error("로그아웃 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "로그아웃 중 오류가 발생했습니다."));
        }
    }
    
    // 토큰 검증 API
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "토큰이 없습니다."));
            }
            
            String token = authHeader.substring(7);
            
            if (jwtProcessor.validateToken(token)) {
                String username = jwtProcessor.getUsername(token);
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", username
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "유효하지 않은 토큰입니다."));
            }
            
        } catch (Exception e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("valid", false, "error", "토큰 검증 중 오류가 발생했습니다."));
        }
    }
} 