package org.ozea.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.point.dto.PointDTO;
import org.ozea.security.util.JwtProcessor;
import org.ozea.user.dto.UserDTO;
import org.ozea.user.dto.UserSignupDTO;
import org.ozea.user.service.UserService;
import org.ozea.point.service.PointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
    final PointService pointService;

    private final Map<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastAttemptTime = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long ATTEMPT_WINDOW = 5 * 60 * 1000;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        if (isRateLimited(email)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "너무 많은 로그인 시도가 있었습니다. 5분 후에 다시 시도해주세요.");
            response.put("error", "RATE_LIMITED");

            return ResponseEntity.status(429).body(response);
        }

        try {
            UserDTO user = service.login(email, password);

            String token = jwtProcessor.generateToken(user.getEmail());

            resetRateLimit(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            response.put("message", "로그인 성공");
            response.put("token", token);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 300);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            incrementRateLimit(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "로그인 실패: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

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

    private void incrementRateLimit(String email) {
        AtomicInteger attempts = loginAttempts.computeIfAbsent(email, k -> new AtomicInteger(0));
        attempts.incrementAndGet();
        lastAttemptTime.put(email, System.currentTimeMillis());
    }

    private void resetRateLimit(String email) {
        loginAttempts.remove(email);
        lastAttemptTime.remove(email);
    }

    private UUID validateAndParseUserId(String userIdStr) {
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 ID가 null이거나 비어있습니다.");
        }

        try {
            return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("사용자 ID 형식이 올바르지 않습니다.");
        }
    }

    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    @GetMapping("/signup/check/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.checkEmail(email));
    }

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

    @PostMapping("/signup/kakao")
    public ResponseEntity<?> signupKakao(@RequestBody UserSignupDTO user) {
        try {
    
            UserDTO result = service.signupKakao(user);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카카오 회원가입 실패");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("카카오 회원가입 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("카카오 회원가입 중 예외 발생: " + e.getMessage());
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.getUserByEmail(email));
    }

    @PostMapping("/verify-user")
    public ResponseEntity<Map<String, Object>> verifyUser(@RequestBody Map<String, String> request) {
        String phoneNum = request.get("phoneNum");
        String email = request.get("email");
        
        boolean success = service.verifyUserInfo(phoneNum, email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        boolean success = service.sendVerificationCode(email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/send-verification-code")
    public ResponseEntity<Map<String, Object>> sendSignupVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        boolean success = service.sendSignupVerificationCode(email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/verify-code")
    public ResponseEntity<Map<String, Object>> verifySignupCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        
        boolean success = service.verifySignupCode(email, code);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        
        boolean success = service.verifyCode(email, code);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");
        
        boolean success = service.changePassword(email, newPassword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }

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
            
    
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "토큰 갱신 실패: " + e.getMessage());
            
            return ResponseEntity.status(401).body(response);
        }
    }

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

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestHeader("Authorization") String authHeader, 
                                                           @RequestBody Map<String, Object> request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("유효하지 않은 토큰입니다.");
            }
            
            String token = authHeader.substring(7);
            String email = jwtProcessor.getUsername(token);

            UserDTO currentUser = service.getUserByEmail(email);
            if (currentUser == null) {
                throw new RuntimeException("사용자를 찾을 수 없습니다.");
            }

            String name = (String) request.get("name");
            String mbti = (String) request.get("mbti");
            String phoneNum = (String) request.get("phoneNum");
            String birthDateStr = (String) request.get("birthDate");
            String sex = (String) request.get("sex");
            Long salary = request.get("salary") != null ? Long.valueOf(request.get("salary").toString()) : null;
            Long payAmount = request.get("payAmount") != null ? Long.valueOf(request.get("payAmount").toString()) : null;

            if (name == null || name.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "이름은 필수 입력 항목입니다.");
                return ResponseEntity.badRequest().body(response);
            }

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

            UserDTO updatedUser = service.updateUserProfile(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필 업데이트 성공");
            response.put("user", updatedUser);
            
    
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("프로필 업데이트 실패: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "프로필 업데이트 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyInfo(@RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);
            String email = jwtProcessor.getUsername(token);

            UserDTO user = service.getUserByEmail(email);
            
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

    @PutMapping("/asset")
    public ResponseEntity<Map<String, Object>> updateAssetInfo(@RequestHeader("Authorization") String authHeader,
                                                              @RequestBody Map<String, Object> request) {

        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("❌ Authorization 헤더가 올바르지 않습니다: {}", authHeader);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "인증 정보가 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            String token = authHeader.substring(7);
            
            String email = jwtProcessor.getUsername(token);

            UserDTO user = service.getUserByEmail(email);
            if (user == null) {
                log.error("❌ 이메일로 사용자를 찾을 수 없습니다: {}", email);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다. 이메일: " + email);
                return ResponseEntity.badRequest().body(response);
            }
            
            UUID userId;
            try {
                userId = validateAndParseUserId(user.getUserId());
            } catch (IllegalArgumentException e) {
                log.error("사용자 ID 검증 실패: {}", e.getMessage());
                return ResponseEntity.badRequest().body(createResponse(false, e.getMessage(), null));
            }
            
            Long salary = request.get("salary") != null ? Long.valueOf(request.get("salary").toString()) : 0L;
            Long payAmount = request.get("payAmount") != null ? Long.valueOf(request.get("payAmount").toString()) : 0L;

            if (salary < 0 || payAmount < 0) {
                log.error("자산정보가 음수입니다: salary={}, payAmount={}", salary, payAmount);
                return ResponseEntity.badRequest().body(createResponse(false, "자산정보는 0 이상이어야 합니다.", null));
            }
            
            UserDTO updatedUser = service.updateAssetInfo(userId, salary, payAmount);

            
            return ResponseEntity.ok(createResponse(true, "자산정보가 성공적으로 수정되었습니다.", updatedUser));
        } catch (Exception e) {
            log.error("자산정보 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(createResponse(false, "자산정보 수정에 실패했습니다: " + e.getMessage(), null));
        }
    }

    @PutMapping("/mbti")
    public ResponseEntity<Map<String, Object>> updateMbti(@RequestHeader("Authorization") String authHeader,
                                                          @RequestBody Map<String, String> request) {
        try {
            String token = authHeader.substring(7);
            String email = jwtProcessor.getUsername(token);

            UserDTO user = service.getUserByEmail(email);

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
            
            UserDTO updatedUser = service.updateMbti(userId, mbti);
            
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

    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestHeader("Authorization") String authHeader,
                                                             @RequestBody Map<String, String> request) {

        try {
            String token = authHeader.substring(7);
            String email = jwtProcessor.getUsername(token);

            UserDTO user = service.getUserByEmail(email);

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

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                log.error("❌ 현재 비밀번호가 null이거나 비어있습니다");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "현재 비밀번호는 필수 입력 항목입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                log.error("❌ 새 비밀번호가 null이거나 비어있습니다");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "새 비밀번호는 필수 입력 항목입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean success = service.updatePasswordWithCurrentCheck(userId, currentPassword, newPassword);
            
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

    @DeleteMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdrawUser(@RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7); // "Bearer " 제거
            String email = jwtProcessor.getUsername(token);

            UserDTO user = service.getUserByEmail(email);

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
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "회원 탈퇴가 성공적으로 처리되었습니다." : "회원 탈퇴 처리에 실패했습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("회원 탈퇴 실패: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "회원 탈퇴 처리에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }



    @GetMapping("/points")
    public ResponseEntity<Map<String, Object>> getMyPoints(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtProcessor.getUsername(token);
            UserDTO user = service.getUserByEmail(email);
            Integer totalPoints = pointService.getTotalPoints(UUID.fromString(user.getUserId()));
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalPoints", totalPoints);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("포인트 조회 실패: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "포인트 조회 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/points/history")
    public ResponseEntity<Map<String, Object>> getMyPointHistory(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtProcessor.getUsername(token);
            UserDTO user = service.getUserByEmail(email);
            List<PointDTO> history = pointService.getPointHistory(UUID.fromString(user.getUserId()));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("history", history);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("포인트 내역 조회 실패: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "포인트 내역 조회 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
