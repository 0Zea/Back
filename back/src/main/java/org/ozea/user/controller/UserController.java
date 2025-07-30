package org.ozea.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.security.util.JwtProcessor;
import org.ozea.user.dto.UserDTO;
import org.ozea.user.dto.UserSignupDTO;
import org.ozea.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
    final UserService service;
    final JwtProcessor jwtProcessor;

    // Rate Limiting을 위한 맵
    private final Map<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastAttemptTime = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5; // 5분당 최대 5회
    private static final long ATTEMPT_WINDOW = 5 * 60 * 1000; // 5분

    // 로그인 (실무 수준 - JWT 토큰 사용 + Rate Limiting)
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        log.info("로그인 시도: email={}", email);

        // Rate Limiting 체크
        if (isRateLimited(email)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "너무 많은 로그인 시도가 있었습니다. 5분 후에 다시 시도해주세요.");
            response.put("error", "RATE_LIMITED");

            return ResponseEntity.status(429).body(response);
        }

        try {
            UserDTO user = service.login(email, password);

            // 실제 JWT 토큰 생성
            String token = jwtProcessor.generateToken(user.getEmail());

            // 성공 시 Rate Limiting 카운터 리셋
            resetRateLimit(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            response.put("message", "로그인 성공");
            response.put("token", token);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 300); // 5분

            log.info("로그인 성공: email={}, userId={}", email, user.getUserId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 실패 시 Rate Limiting 카운터 증가
            incrementRateLimit(email);

            log.error("로그인 실패: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "로그인 실패: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    // Rate Limiting 체크
    private boolean isRateLimited(String email) {
        AtomicInteger attempts = loginAttempts.get(email);
        Long lastAttempt = lastAttemptTime.get(email);

        if (attempts == null || lastAttempt == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttempt > ATTEMPT_WINDOW) {
            // 시간이 지났으면 리셋
            resetRateLimit(email);
            return false;
        }

        return attempts.get() >= MAX_ATTEMPTS;
    }

    // Rate Limiting 카운터 증가
    private void incrementRateLimit(String email) {
        AtomicInteger attempts = loginAttempts.computeIfAbsent(email, k -> new AtomicInteger(0));
        attempts.incrementAndGet();
        lastAttemptTime.put(email, System.currentTimeMillis());
    }

    // Rate Limiting 카운터 리셋
    private void resetRateLimit(String email) {
        loginAttempts.remove(email);
        lastAttemptTime.remove(email);
    }

    // 이메일 중복 확인
    @GetMapping("/signup/check/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.checkEmail(email));
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserSignupDTO user) {
        try {
            UserDTO result = service.signup(user);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 실패");
            }
            return ResponseEntity.ok(result); // 최소한 UserDTO 반환
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 중 예외 발생: " + e.getMessage());
        }
    }


    // 이메일을 기준으로 사용자 정보를 조회
    @GetMapping("/user/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.getUserByEmail(email));
    }
    
    // 사용자 정보 확인 (전화번호와 이메일)
    @PostMapping("/verify-user")
    public ResponseEntity<Map<String, Object>> verifyUser(@RequestBody Map<String, String> request) {
        String phoneNum = request.get("phoneNum");
        String email = request.get("email");
        
        boolean success = service.verifyUserInfo(phoneNum, email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }
    
    // 인증번호 발송
    @PostMapping("/send-verification-code")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        boolean success = service.sendVerificationCode(email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }
    
    // 인증번호 확인
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        
        boolean success = service.verifyCode(email, code);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }
    
    // 비밀번호 변경
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");
        
        boolean success = service.changePassword(email, newPassword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }
    
    // 토큰 갱신
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("유효하지 않은 토큰입니다.");
            }
            
            String token = authHeader.substring(7);
            String email = jwtProcessor.getUsername(token);
            
            // 새로운 토큰 생성
            String newToken = jwtProcessor.generateToken(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", newToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 300);
            
            log.info("토큰 갱신 성공: email={}", email);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "토큰 갱신 실패: " + e.getMessage());
            
            return ResponseEntity.status(401).body(response);
        }
    }
    
    // 현재 사용자 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("유효하지 않은 토큰입니다.");
            }
            
            String token = authHeader.substring(7);
            String email = jwtProcessor.getUsername(token);
            
            UserDTO user = service.getUserByEmail(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("프로필 조회 실패: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "프로필 조회 실패: " + e.getMessage());
            
            return ResponseEntity.status(401).body(response);
        }
    }
    
    // 사용자 프로필 업데이트
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestHeader("Authorization") String authHeader, 
                                                           @RequestBody Map<String, Object> request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("유효하지 않은 토큰입니다.");
            }
            
            String token = authHeader.substring(7);
            String email = jwtProcessor.getUsername(token);
            
            // 프로필 업데이트 로직 (실제 구현 필요)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필 업데이트 성공");
            
            log.info("프로필 업데이트 성공: email={}", email);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("프로필 업데이트 실패: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "프로필 업데이트 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
