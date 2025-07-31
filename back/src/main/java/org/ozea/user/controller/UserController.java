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
import org.ozea.user.domain.User;
import java.util.UUID;

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
    
    // 현재 사용자 프로필 조회 (프론트엔드 호환성)
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
            
            // 현재 사용자 정보 조회
            UserDTO currentUser = service.getUserByEmail(email);
            if (currentUser == null) {
                throw new RuntimeException("사용자를 찾을 수 없습니다.");
            }
            
            // 업데이트할 필드들 추출
            String name = (String) request.get("name");
            String mbti = (String) request.get("mbti");
            String phoneNum = (String) request.get("phoneNum");
            String birthDateStr = (String) request.get("birthDate");
            String sex = (String) request.get("sex");
            Long salary = request.get("salary") != null ? Long.valueOf(request.get("salary").toString()) : null;
            Long payAmount = request.get("payAmount") != null ? Long.valueOf(request.get("payAmount").toString()) : null;
            
            // 필수 필드 검증
            if (name == null || name.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "이름은 필수 입력 항목입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // User 객체 생성 및 업데이트
            User user = currentUser.toVO();
            if (name != null) user.setName(name);
            if (mbti != null) user.setMbti(mbti);
            if (phoneNum != null) user.setPhoneNum(phoneNum);
            if (birthDateStr != null) {
                user.setBirthDate(java.time.LocalDate.parse(birthDateStr));
            }
            if (sex != null) user.setSex(sex);
            if (salary != null) user.setSalary(salary);
            if (payAmount != null) user.setPayAmount(payAmount);
            
            // DB 업데이트
            UserDTO updatedUser = service.updateUserProfile(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필 업데이트 성공");
            response.put("user", updatedUser);
            
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

    // 마이페이지 - 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyInfo(@RequestHeader("Authorization") String authHeader) {
        log.info("🔍 내 정보 조회 요청: Authorization={}", authHeader);
        try {
            // JWT 토큰에서 사용자 이메일 추출
            String token = authHeader.substring(7); // "Bearer " 제거
            String email = jwtProcessor.getUsername(token);
            log.info("📧 JWT에서 추출된 이메일: {}", email);
            
            // 이메일로 사용자 정보 조회
            UserDTO user = service.getUserByEmail(email);
            log.info("✅ 사용자 정보 조회 성공: userId={}", user.getUserId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "내 정보 조회 성공");
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 내 정보 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "내 정보 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 마이페이지 - 자산정보 수정
    @PutMapping("/asset")
    public ResponseEntity<Map<String, Object>> updateAssetInfo(@RequestHeader("Authorization") String authHeader,
                                                              @RequestBody Map<String, Object> request) {
        log.info("💰 자산정보 수정 요청: Authorization={}", authHeader);
        log.info("💰 요청 데이터: {}", request);
        
        try {
            // JWT 토큰에서 사용자 이메일 추출
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("❌ Authorization 헤더가 올바르지 않습니다: {}", authHeader);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "인증 정보가 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            String token = authHeader.substring(7); // "Bearer " 제거
            log.info("🔐 추출된 토큰: {}", token.substring(0, Math.min(50, token.length())) + "...");
            
            String email = jwtProcessor.getUsername(token);
            log.info("📧 JWT에서 추출된 이메일: {}", email);
            
            // 이메일로 사용자 정보 조회
            UserDTO user = service.getUserByEmail(email);
            if (user == null) {
                log.error("❌ 이메일로 사용자를 찾을 수 없습니다: {}", email);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다. 이메일: " + email);
                return ResponseEntity.badRequest().body(response);
            }
            
            // userId가 null이거나 잘못된 형식인지 확인
            if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
                log.error("❌ 사용자 ID가 null이거나 비어있습니다: {}", user.getUserId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "사용자 ID가 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            UUID userId;
            try {
                userId = UUID.fromString(user.getUserId());
                log.info("👤 사용자 정보 조회 성공: userId={}", userId);
            } catch (IllegalArgumentException e) {
                log.error("❌ 잘못된 UUID 형식입니다: {}", user.getUserId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "사용자 ID 형식이 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Long salary = request.get("salary") != null ? Long.valueOf(request.get("salary").toString()) : 0L;
            Long payAmount = request.get("payAmount") != null ? Long.valueOf(request.get("payAmount").toString()) : 0L;
            log.info("💵 자산정보: salary={}, payAmount={}", salary, payAmount);
            
            // 자산정보 유효성 검증
            if (salary < 0 || payAmount < 0) {
                log.error("❌ 자산정보가 음수입니다: salary={}, payAmount={}", salary, payAmount);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "자산정보는 0 이상이어야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            UserDTO updatedUser = service.updateAssetInfo(userId, salary, payAmount);
            log.info("✅ 자산정보 수정 성공: userId={}", userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "자산정보가 성공적으로 수정되었습니다.");
            response.put("user", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 자산정보 수정 실패: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "자산정보 수정에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 마이페이지 - MBTI 수정
    @PutMapping("/mbti")
    public ResponseEntity<Map<String, Object>> updateMbti(@RequestHeader("Authorization") String authHeader,
                                                          @RequestBody Map<String, String> request) {
        log.info("🧠 MBTI 수정 요청: Authorization={}, request={}", authHeader, request);
        try {
            // JWT 토큰에서 사용자 이메일 추출
            String token = authHeader.substring(7); // "Bearer " 제거
            String email = jwtProcessor.getUsername(token);
            log.info("📧 JWT에서 추출된 이메일: {}", email);
            
            // 이메일로 사용자 정보 조회
            UserDTO user = service.getUserByEmail(email);
            
            // userId가 null이거나 잘못된 형식인지 확인
            if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
                log.error("❌ 사용자 ID가 null이거나 비어있습니다: {}", user.getUserId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "사용자 ID가 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            UUID userId;
            try {
                userId = UUID.fromString(user.getUserId());
                log.info("👤 사용자 정보 조회 성공: userId={}", userId);
            } catch (IllegalArgumentException e) {
                log.error("❌ 잘못된 UUID 형식입니다: {}", user.getUserId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "사용자 ID 형식이 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            String mbti = request.get("mbti");
            if (mbti == null || mbti.trim().isEmpty()) {
                log.error("❌ MBTI가 null이거나 비어있습니다: {}", mbti);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "MBTI는 필수 입력 항목입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            log.info("🧠 MBTI: {}", mbti);
            
            UserDTO updatedUser = service.updateMbti(userId, mbti);
            log.info("✅ MBTI 수정 성공: userId={}, mbti={}", userId, mbti);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "MBTI가 성공적으로 수정되었습니다.");
            response.put("user", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ MBTI 수정 실패: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "MBTI 수정에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 마이페이지 - 비밀번호 수정
    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestHeader("Authorization") String authHeader,
                                                             @RequestBody Map<String, String> request) {
        log.info("🔐 비밀번호 수정 요청: Authorization={}", authHeader);
        try {
            // JWT 토큰에서 사용자 이메일 추출
            String token = authHeader.substring(7); // "Bearer " 제거
            String email = jwtProcessor.getUsername(token);
            log.info("📧 JWT에서 추출된 이메일: {}", email);
            
            // 이메일로 사용자 정보 조회
            UserDTO user = service.getUserByEmail(email);
            
            // userId가 null이거나 잘못된 형식인지 확인
            if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
                log.error("❌ 사용자 ID가 null이거나 비어있습니다: {}", user.getUserId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "사용자 ID가 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            UUID userId;
            try {
                userId = UUID.fromString(user.getUserId());
                log.info("👤 사용자 정보 조회 성공: userId={}", userId);
            } catch (IllegalArgumentException e) {
                log.error("❌ 잘못된 UUID 형식입니다: {}", user.getUserId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "사용자 ID 형식이 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            
            // 현재 비밀번호 검증
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                log.error("❌ 현재 비밀번호가 null이거나 비어있습니다");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "현재 비밀번호는 필수 입력 항목입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 새 비밀번호 검증
            if (newPassword == null || newPassword.trim().isEmpty()) {
                log.error("❌ 새 비밀번호가 null이거나 비어있습니다");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "새 비밀번호는 필수 입력 항목입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("🔐 현재 비밀번호 길이: {}, 새 비밀번호 길이: {}", currentPassword.length(), newPassword.length());
            
            boolean success = service.updatePasswordWithCurrentCheck(userId, currentPassword, newPassword);
            log.info("✅ 비밀번호 수정 성공: userId={}", userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "비밀번호가 성공적으로 수정되었습니다." : "현재 비밀번호가 일치하지 않습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 비밀번호 수정 실패: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "비밀번호 수정에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 마이페이지 - 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdrawUser(@RequestHeader("Authorization") String authHeader) {
        log.info("🚪 회원 탈퇴 요청: Authorization={}", authHeader);
        try {
            // JWT 토큰에서 사용자 이메일 추출
            String token = authHeader.substring(7); // "Bearer " 제거
            String email = jwtProcessor.getUsername(token);
            log.info("📧 JWT에서 추출된 이메일: {}", email);
            
            // 이메일로 사용자 정보 조회
            UserDTO user = service.getUserByEmail(email);
            
            // userId가 null이거나 잘못된 형식인지 확인
            if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
                log.error("❌ 사용자 ID가 null이거나 비어있습니다: {}", user.getUserId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "사용자 ID가 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            UUID userId;
            try {
                userId = UUID.fromString(user.getUserId());
                log.info("👤 사용자 정보 조회 성공: userId={}", userId);
            } catch (IllegalArgumentException e) {
                log.error("❌ 잘못된 UUID 형식입니다: {}", user.getUserId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "사용자 ID 형식이 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean success = service.withdrawUser(userId);
            log.info("✅ 회원 탈퇴 성공: userId={}", userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "회원 탈퇴가 성공적으로 처리되었습니다." : "회원 탈퇴 처리에 실패했습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 회원 탈퇴 실패: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "회원 탈퇴 처리에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 테스트용 사용자 생성 (개발 중에만 사용)
    @PostMapping("/test-user")
    public ResponseEntity<Map<String, Object>> createTestUser() {
        try {
            // 테스트용 사용자 정보
            UserSignupDTO testUser = UserSignupDTO.builder()
                    .name("테스트 사용자")
                    .email("test@example.com")
                    .password("Test123!@#")
                    .phoneNum("010-1234-5678")
                    .birthDate(java.time.LocalDate.of(1990, 1, 1))
                    .sex("male")
                    .salary(3000000L)
                    .payAmount(1000000L)
                    .mbti("신중한 분석가")
                    .build();
            
            UserDTO createdUser = service.signup(testUser);
            
            // JWT 토큰 생성
            String token = jwtProcessor.generateToken(createdUser.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "테스트 사용자가 생성되었습니다.");
            response.put("user", createdUser);
            response.put("token", token);
            
            log.info("✅ 테스트 사용자 생성 성공: email={}, token={}", createdUser.getEmail(), token.substring(0, 50) + "...");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 테스트 사용자 생성 실패: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "테스트 사용자 생성에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
