package org.ozea.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.user.domain.User;
import org.ozea.user.mapper.UserMapper;
import org.ozea.user.dto.UserDTO;
import org.ozea.user.dto.UserSignupDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    final UserMapper mapper;
    final PasswordEncoder passwordEncoder;
    final VerificationCodeService verificationCodeService;
    final EmailService emailService;

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
        validatePassword(dto.getPassword());
        
        // DTO → VO
        User user = dto.toVO();

        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // DB 저장
        mapper.insertUser(user);

        // 저장된 사용자 정보 조회 후 DTO 반환
        return getUserByEmail(user.getEmail());
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
    @Transactional
    public boolean changePassword(String email, String newPassword) {
        try {
            User user = mapper.getUserByEmail(email);
            if (user == null) {
                return false;
            }
            
            // 새 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            
            // DB 업데이트
            mapper.updateUser(user);
            
            return true;
        } catch (Exception e) {
            log.error("비밀번호 변경 실패: {}", e.getMessage());
            return false;
        }
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
}
