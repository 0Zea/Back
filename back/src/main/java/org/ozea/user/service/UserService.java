package org.ozea.user.service;

import org.ozea.user.domain.User;
import org.ozea.user.dto.UserDTO;
import org.ozea.user.dto.UserSignupDTO;
import org.ozea.user.dto.PasswordResetDTO;
import org.ozea.user.dto.VerificationDTO;

public interface UserService {
    boolean checkEmail(String email);       // 이메일 중복 확인
    UserDTO signup(UserSignupDTO dto);      // 회원가입
    UserDTO getUserByEmail(String email);   // 이메일을 기준으로 사용자 정보를 조회
    UserDTO login(String email, String password); // 로그인
    
    // 비밀번호 찾기 관련 메서드들
    boolean verifyUserInfo(String phoneNum, String email);  // 전화번호와 이메일로 사용자 확인
    boolean sendVerificationCode(String email);             // 인증번호 발송
    boolean verifyCode(String email, String code);          // 인증번호 확인
    boolean changePassword(String email, String newPassword); // 비밀번호 변경
}
