package org.ozea.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.security.client.KakaoApiClient;
import org.ozea.user.domain.User;
import org.ozea.user.dto.UserDTO;
import org.ozea.user.dto.UserSignupDTO;
import org.ozea.user.mapper.UserMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;
    private final KakaoApiClient kakaoApiClient;
    private final CacheManager cacheManager;

    // ====== 조회 (Cacheable) ======

    @Override
    @Cacheable(value = "userByEmail", key = "#p0", unless = "#result == null")
    public UserDTO getUserByEmail(String email) {
        User user = mapper.getUserByEmail(email);
        if (user == null) return null;
        return UserDTO.of(user);
    }

    @Override
    @Cacheable(cacheNames = "userById", key = "#p0", unless = "#result == null")
    public UserDTO getMyInfo(UUID userId) {
        User user = mapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        return UserDTO.of(user);
    }

    // ====== 가입/수정 (리턴 DTO 기반 캐시 무효화) ======

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "userById", key = "#result.userId",   condition = "#result != null"),
            @CacheEvict(cacheNames = "userByEmail", key = "#result.email", condition = "#result != null")
    })
    public UserDTO signup(UserSignupDTO dto) {
        if (!dto.isKakao()) {
            validatePassword(dto.getPassword());
        }
        User user = dto.toVO();
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            user.setPassword(null);
        }
        mapper.insertUser(user);
        return getUserByEmail(user.getEmail());
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "userById", key = "#result.userId",   condition = "#result != null"),
            @CacheEvict(cacheNames = "userByEmail", key = "#result.email", condition = "#result != null")
    })
    public UserDTO signupKakao(UserSignupDTO dto) {
        User existingUser = mapper.getUserByEmail(dto.getEmail());
        if (existingUser == null) {
            existingUser = new User();
            existingUser.setUserId(UUID.randomUUID());
            existingUser.setEmail(dto.getEmail());
            existingUser.setName(dto.getName());
            existingUser.setPhoneNum(dto.getPhoneNum());
            existingUser.setBirthDate(dto.getBirthDate());
            existingUser.setSex(dto.getSex());
            existingUser.setSalary(dto.getSalary());
            existingUser.setPayAmount(dto.getPayAmount());
            existingUser.setMbti(dto.getMbti());
            existingUser.setRole("USER");
            existingUser.setPassword("");
            mapper.insertUser(existingUser);
        } else {
            existingUser.setName(dto.getName());
            existingUser.setPhoneNum(dto.getPhoneNum());
            existingUser.setBirthDate(dto.getBirthDate());
            existingUser.setSex(dto.getSex());
            existingUser.setSalary(dto.getSalary());
            existingUser.setPayAmount(dto.getPayAmount());
            existingUser.setMbti(dto.getMbti());
            mapper.updateUser(existingUser);
        }
        return getUserByEmail(existingUser.getEmail());
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "userById", key = "#result.userId",   condition = "#result != null"),
            @CacheEvict(cacheNames = "userByEmail", key = "#result.email", condition = "#result != null")
    })
    public UserDTO updateUserProfile(User user) {
        User existingUser = mapper.getUserByEmail(user.getEmail());
        if (existingUser == null) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다.");
        }
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getMbti() != null) {
            existingUser.setMbti(user.getMbti());
        }
        if (user.getPhoneNum() != null) {
            existingUser.setPhoneNum(user.getPhoneNum());
        }
        if (user.getBirthDate() != null) {
            existingUser.setBirthDate(user.getBirthDate());
        }
        if (user.getSex() != null) {
            existingUser.setSex(user.getSex());
        }
        if (user.getSalary() != null) {
            existingUser.setSalary(user.getSalary());
        }
        if (user.getPayAmount() != null) {
            existingUser.setPayAmount(user.getPayAmount());
        }
        mapper.updateUser(existingUser);
        return getUserByEmail(user.getEmail());
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "userById", key = "#result.userId",   condition = "#result != null"),
            @CacheEvict(cacheNames = "userByEmail", key = "#result.email", condition = "#result != null")
    })
    public UserDTO updateAssetInfo(UUID userId, Long salary, Long payAmount) {
        User user = mapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        user.setSalary(salary);
        user.setPayAmount(payAmount);
        mapper.updateUser(user);
        return UserDTO.of(user);
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "userById", key = "#result.userId",   condition = "#result != null"),
            @CacheEvict(cacheNames = "userByEmail", key = "#result.email", condition = "#result != null")
    })
    public UserDTO updateMbti(UUID userId, String mbti) {
        User user = mapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        if (mbti == null || mbti.trim().isEmpty()) {
            throw new RuntimeException("MBTI는 필수 입력 항목입니다.");
        }
        user.setMbti(mbti);
        mapper.updateUser(user);
        return UserDTO.of(user);
    }

    // ====== 비밀번호/탈퇴 (프로그램 방식 캐시 무효화) ======

    @Override
    public boolean changePassword(String email, String newPassword) {
        validatePassword(newPassword);
        User user = mapper.getUserByEmail(email);
        if (user == null) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다.");
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        mapper.updateUser(user);
        evictUserCaches(user);
        return true;
    }

    @Transactional
    @Override
    public boolean updatePassword(UUID userId, String newPassword) {
        User user = mapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        mapper.updateUser(user);
        evictUserCaches(user);
        return true;
    }

    @Transactional
    @Override
    public boolean updatePasswordWithCurrentCheck(UUID userId, String currentPassword, String newPassword) {
        User user = mapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        boolean currentPasswordMatches =
                passwordEncoder.matches(currentPassword, user.getPassword()) ||
                        currentPassword.equals(user.getPassword());
        if (!currentPasswordMatches) {
            log.warn("현재 비밀번호가 일치하지 않습니다: userId={}", userId);
            return false;
        }
        if (currentPassword.equals(newPassword)) {
            throw new RuntimeException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }
        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        mapper.updateUser(user);
        evictUserCaches(user);
        return true;
    }

    @Transactional
    @Override
    public boolean withdrawUser(UUID userId) {
        User user = mapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        try {
            try {
                unlinkKakaoAccount(user);
            } catch (Exception e) {
                log.warn("카카오 연동 해제 실패: {}", e.getMessage());
            }
            try {
                mapper.deleteUserPoints(userId);
            } catch (Exception e) {
                log.warn("포인트 내역 삭제 실패: {}", e.getMessage());
            }
            try {
                mapper.deleteUserInquiries(userId);
            } catch (Exception e) {
                log.warn("문의 내역 삭제 실패: {}", e.getMessage());
            }
            try {
                mapper.deleteUserQuiz(userId);
            } catch (Exception e) {
                log.warn("퀴즈 내역 삭제 실패: {}", e.getMessage());
            }
            try {
                mapper.deleteUserGoals(userId);
            } catch (Exception e) {
                log.warn("목표 정보 삭제 실패: {}", e.getMessage());
            }
            mapper.deleteUserData(userId);
            evictUserCaches(user);
            return true;
        } catch (Exception e) {
            log.error("회원 탈퇴 실패: userId={}, error={}", userId, e.getMessage());
            throw new RuntimeException("회원 탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // ====== 인증/검증(이메일 코드) ======

    @Override
    public boolean checkEmail(String email) {
        return mapper.checkEmail(email);
    }

    @Override
    public UserDTO login(String email, String password) {
        User user = mapper.getUserByEmail(email);
        if (user == null) {
            log.warn("로그인 실패: 사용자를 찾을 수 없음 - email={}", email);
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        boolean passwordMatches =
                passwordEncoder.matches(password, user.getPassword()) ||
                        password.equals(user.getPassword());
        if (!passwordMatches) {
            log.warn("로그인 실패: 비밀번호 불일치 - email={}", email);
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return UserDTO.of(user);
    }

    @Override
    public boolean verifyUserInfo(String phoneNum, String email) {
        User user = mapper.getUserByEmail(email);
        if (user == null) {
            return false;
        }
        return phoneNum.equals(user.getPhoneNum());
    }

    @Override
    public boolean sendVerificationCode(String email) {
        try {
            String code = generateVerificationCode();
            verificationCodeService.saveVerificationCode(email, code);
            emailService.sendVerificationEmail(email, code);
            return true;
        } catch (Exception e) {
            log.error("인증번호 발송 실패: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        return verificationCodeService.verifyCode(email, code);
    }

    @Override
    public boolean sendSignupVerificationCode(String email) {
        try {
            if (mapper.checkEmail(email)) {
                log.warn("이미 존재하는 이메일: {}", email);
                return false;
            }
            String code = generateVerificationCode();
            verificationCodeService.saveVerificationCode(email, code);
            emailService.sendSignupVerificationEmail(email, code);
            return true;
        } catch (Exception e) {
            log.error("회원가입 인증번호 발송 실패: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean verifySignupCode(String email, String code) {
        return verificationCodeService.verifyCode(email, code);
    }

    // ====== 내부 유틸 ======

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        if (!hasLetter || !hasDigit || !hasSpecial) {
            throw new RuntimeException("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private void unlinkKakaoAccount(User user) {
        try {
            String accessToken = user.getKakaoAccessToken();
            if (accessToken != null && !accessToken.isEmpty()) {
                boolean success = kakaoApiClient.unlink(accessToken);
                if (!success) {
                    log.warn("카카오 연동 해제 실패: email={}", user.getEmail());
                }
            }
        } catch (Exception e) {
            log.warn("카카오 연동 해제 중 오류: email={}, error={}", user.getEmail(), e.getMessage());
        }
    }

    private void evictUserCaches(User user) {
        if (user == null) {
            return;
        }
        if (cacheManager.getCache("userById") != null) {
            cacheManager.getCache("userById").evict(user.getUserId());
        }
        if (cacheManager.getCache("userByEmail") != null && user.getEmail() != null) {
            cacheManager.getCache("userByEmail").evict(user.getEmail());
        }
    }
}