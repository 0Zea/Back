package org.ozea.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.user.domain.User;
import org.ozea.user.mapper.UserMapper;
import org.ozea.user.dto.UserDTO;
import org.ozea.user.dto.UserSignupDTO;
import org.ozea.security.client.KakaoApiClient;
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
    final UserMapper mapper;
    final PasswordEncoder passwordEncoder;
    final VerificationCodeService verificationCodeService;
    final EmailService emailService;
    final KakaoApiClient kakaoApiClient;

    // 이메일 중복 확인
    @Override
    public boolean checkEmail(String email) {
        return mapper.checkEmail(email);
    }

    // 회원가입
    @Transactional
    @Override
    public UserDTO signup(UserSignupDTO dto) {
        // 비밀번호 정책 검증
//        validatePassword(dto.getPassword());

        // 로컬 유저인 경우에만 비밀번호 검증
        if (!dto.isKakao()) {
            validatePassword(dto.getPassword());
        }

        // DTO → VO
        User user = dto.toVO();

        // 비밀번호 암호화
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            user.setPassword(null);
        }

        // DB 저장
        mapper.insertUser(user);

        // 저장된 사용자 정보 조회 후 DTO 반환
        return getUserByEmail(user.getEmail());
    }

    // 카카오 회원가입 (기존 임시 사용자 정보 업데이트)
    @Transactional
    @Override
    public UserDTO signupKakao(UserSignupDTO dto) {
        log.info("카카오 회원가입 서비스 호출: email={}, name={}", dto.getEmail(), dto.getName());
        
        // 기존 카카오 사용자 확인
        User existingUser = mapper.getUserByEmail(dto.getEmail());
        
        if (existingUser == null) {
            // 기존 사용자가 없으면 새로 생성
            log.info("기존 사용자가 없어 새로 생성합니다: email={}", dto.getEmail());
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
            existingUser.setPassword(""); // 카카오 사용자는 비밀번호 없음
            
            // DB에 새 사용자 저장
            mapper.insertUser(existingUser);
            log.info("새 사용자 생성 완료: userId={}", existingUser.getUserId());
        } else {
            log.info("기존 사용자 발견: userId={}", existingUser.getUserId());

            // 기존 사용자 정보 업데이트
            existingUser.setName(dto.getName());
            existingUser.setPhoneNum(dto.getPhoneNum());
            existingUser.setBirthDate(dto.getBirthDate());
            existingUser.setSex(dto.getSex());
            existingUser.setSalary(dto.getSalary());
            existingUser.setPayAmount(dto.getPayAmount());
            existingUser.setMbti(dto.getMbti());

            // DB 업데이트
            mapper.updateUser(existingUser);
            log.info("사용자 정보 업데이트 완료");
        }

        // 업데이트된 사용자 정보 조회 후 DTO 반환
        return getUserByEmail(existingUser.getEmail());
    }
    
    // 비밀번호 정책 검증
    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
        
        // 영문, 숫자, 특수문자 조합 검증
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        
        if (!hasLetter || !hasDigit || !hasSpecial) {
            throw new RuntimeException("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
        }
    }

    // 이메일을 기준으로 사용자 정보를 조회
    @Override
    public UserDTO getUserByEmail(String email) {
        User user = mapper.getUserByEmail(email);
        return UserDTO.of(user); // DTO 변환 메서드 필요
    }
    
    // 로그인
    @Override
    public UserDTO login(String email, String password) {
        log.info("로그인 시도: email={}", email);
        
        User user = mapper.getUserByEmail(email);
        if (user == null) {
            log.warn("로그인 실패: 사용자를 찾을 수 없음 - email={}", email);
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        // 비밀번호 확인 (BCrypt와 평문 모두 허용)
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword()) || 
                                password.equals(user.getPassword());
        
        if (!passwordMatches) {
            log.warn("로그인 실패: 비밀번호 불일치 - email={}", email);
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        log.info("로그인 성공: email={}, userId={}", email, user.getUserId());
        return UserDTO.of(user);
    }
    
    // 전화번호와 이메일로 사용자 확인
    @Override
    public boolean verifyUserInfo(String phoneNum, String email) {
        User user = mapper.getUserByEmail(email);
        if (user == null) {
            return false;
        }
        return phoneNum.equals(user.getPhoneNum());
    }
    
    // 인증번호 발송
    @Override
    public boolean sendVerificationCode(String email) {
        try {
            // 6자리 랜덤 인증번호 생성
            String code = generateVerificationCode();
            
            // 인증번호 저장 (5분 만료)
            verificationCodeService.saveVerificationCode(email, code);
            
            // 실제 이메일 발송
            emailService.sendVerificationEmail(email, code);
            
            return true;
        } catch (Exception e) {
            log.error("인증번호 발송 실패: {}", e.getMessage());
            return false;
        }
    }
    
    // 인증번호 확인
    @Override
    public boolean verifyCode(String email, String code) {
        return verificationCodeService.verifyCode(email, code);
    }
    
    // 비밀번호 변경
    @Override
    public boolean changePassword(String email, String newPassword) {
        // 비밀번호 정책 검증
        validatePassword(newPassword);
        
        // 사용자 조회
        User user = mapper.getUserByEmail(email);
        if (user == null) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다.");
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        
        // DB 업데이트
        mapper.updateUser(user);
        
        log.info("비밀번호 변경 성공: email={}", email);
        return true;
    }
    
    // 프로필 업데이트
    @Override
    @Transactional
    public UserDTO updateUserProfile(User user) {
        // 사용자 존재 여부 확인
        User existingUser = mapper.getUserByEmail(user.getEmail());
        if (existingUser == null) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다.");
        }
        
        // 업데이트할 필드들 설정
        if (user.getName() != null) existingUser.setName(user.getName());
        if (user.getMbti() != null) existingUser.setMbti(user.getMbti());
        if (user.getPhoneNum() != null) existingUser.setPhoneNum(user.getPhoneNum());
        if (user.getBirthDate() != null) existingUser.setBirthDate(user.getBirthDate());
        if (user.getSex() != null) existingUser.setSex(user.getSex());
        if (user.getSalary() != null) existingUser.setSalary(user.getSalary());
        if (user.getPayAmount() != null) existingUser.setPayAmount(user.getPayAmount());
        
        // DB 업데이트
        mapper.updateUser(existingUser);
        
        log.info("프로필 업데이트 성공: email={}", user.getEmail());
        
        // 업데이트된 사용자 정보 반환
        return getUserByEmail(user.getEmail());
    }
    
    // 6자리 랜덤 인증번호 생성
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    // 마이페이지 - 내 정보 조회
    @Override
    public UserDTO getMyInfo(UUID userId) {
        log.info("내 정보 조회: userId={}", userId);
        User user = mapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        return UserDTO.of(user);
    }

    // 마이페이지 - 자산정보 수정
    @Override
    @Transactional
    public UserDTO updateAssetInfo(UUID userId, Long salary, Long payAmount) {
        log.info("자산정보 수정: userId={}, salary={}, payAmount={}", userId, salary, payAmount);
        
        User user = mapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        // 자산정보 업데이트
        user.setSalary(salary);
        user.setPayAmount(payAmount);
        
        mapper.updateUser(user);
        
        log.info("자산정보 수정 완료: userId={}", userId);
        return UserDTO.of(user);
    }

    // 마이페이지 - MBTI 수정
    @Override
    @Transactional
    public UserDTO updateMbti(UUID userId, String mbti) {
        log.info("MBTI 수정: userId={}, mbti={}", userId, mbti);
        
        User user = mapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        // MBTI 유효성 검증
        if (mbti == null || mbti.trim().isEmpty()) {
            throw new RuntimeException("MBTI는 필수 입력 항목입니다.");
        }
        
        // MBTI 업데이트
        user.setMbti(mbti);
        mapper.updateUser(user);
        
        log.info("MBTI 수정 완료: userId={}, mbti={}", userId, mbti);
        return UserDTO.of(user);
    }

    // 마이페이지 - 비밀번호 수정
    @Override
    @Transactional
    public boolean updatePassword(UUID userId, String newPassword) {
        log.info("비밀번호 수정: userId={}", userId);
        
        User user = mapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        // 비밀번호 정책 검증
        validatePassword(newPassword);
        
        // 비밀번호 암호화 후 업데이트
        user.setPassword(passwordEncoder.encode(newPassword));
        mapper.updateUser(user);
        
        log.info("비밀번호 수정 완료: userId={}", userId);
        return true;
    }
    
    // 마이페이지 - 현재 비밀번호 확인 후 비밀번호 수정
    @Override
    @Transactional
    public boolean updatePasswordWithCurrentCheck(UUID userId, String currentPassword, String newPassword) {
        log.info("현재 비밀번호 확인 후 비밀번호 수정: userId={}", userId);
        
        User user = mapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        // 현재 비밀번호 확인
        boolean currentPasswordMatches = passwordEncoder.matches(currentPassword, user.getPassword()) || 
                                       currentPassword.equals(user.getPassword());
        
        if (!currentPasswordMatches) {
            log.warn("현재 비밀번호가 일치하지 않습니다: userId={}", userId);
            return false;
        }
        
        // 새 비밀번호가 현재 비밀번호와 같은지 확인
        if (currentPassword.equals(newPassword)) {
            throw new RuntimeException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }
        
        // 비밀번호 정책 검증
        validatePassword(newPassword);
        
        // 비밀번호 암호화 후 업데이트
        user.setPassword(passwordEncoder.encode(newPassword));
        mapper.updateUser(user);
        
        log.info("비밀번호 수정 완료: userId={}", userId);
        return true;
    }

    // 마이페이지 - 회원 탈퇴
    @Override
    @Transactional
    public boolean withdrawUser(UUID userId) {
        log.info("회원 탈퇴: userId={}", userId);
        
        User user = mapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        try {
            // 카카오 연동 해제 (카카오 로그인 사용자인 경우)
            try {
                unlinkKakaoAccount(user);
            } catch (Exception e) {
                log.warn("카카오 연동 해제 실패: {}", e.getMessage());
            }
            
            // 외래키 제약조건을 고려한 삭제 순서
            // 1. 포인트 내역 삭제
            try {
                mapper.deleteUserPoints(userId);
            } catch (Exception e) {
                log.warn("포인트 내역 삭제 실패: {}", e.getMessage());
            }
            
            // 2. 문의 내역 삭제
            try {
                mapper.deleteUserInquiries(userId);
            } catch (Exception e) {
                log.warn("문의 내역 삭제 실패: {}", e.getMessage());
            }
            
            // 3. 목표 정보 삭제
            try {
                mapper.deleteUserGoals(userId);
            } catch (Exception e) {
                log.warn("목표 정보 삭제 실패: {}", e.getMessage());
            }
            
            // 4. 마지막으로 사용자 정보 삭제
            mapper.deleteUserData(userId);
            
            log.info("회원 탈퇴 완료: userId={}", userId);
            return true;
        } catch (Exception e) {
            log.error("회원 탈퇴 실패: userId={}, error={}", userId, e.getMessage());
            throw new RuntimeException("회원 탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    

    
    // 카카오 연동 해제 (내부 메서드)
    private void unlinkKakaoAccount(User user) {
        try {
            String accessToken = user.getKakaoAccessToken();
            
            // 카카오 액세스 토큰이 있는 경우에만 연동 해제 시도
            if (accessToken != null && !accessToken.isEmpty()) {
                // 카카오 연동 해제 API 호출
                boolean success = kakaoApiClient.unlink(accessToken);
                
                if (!success) {
                    log.warn("카카오 연동 해제 실패: email={}", user.getEmail());
                }
            }
        } catch (Exception e) {
            log.warn("카카오 연동 해제 중 오류: email={}, error={}", user.getEmail(), e.getMessage());
        }
    }
}
