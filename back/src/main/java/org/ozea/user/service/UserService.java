package org.ozea.user.service;

import org.ozea.user.domain.User;
import org.ozea.user.dto.UserDTO;
import org.ozea.user.dto.UserSignupDTO;
import org.ozea.user.dto.PasswordResetDTO;
import org.ozea.user.dto.VerificationDTO;

import java.util.UUID;

public interface UserService {
    boolean checkEmail(String email);
    UserDTO signup(UserSignupDTO dto);
    UserDTO signupKakao(UserSignupDTO dto);
    UserDTO getUserByEmail(String email);
    UserDTO login(String email, String password);
    
    boolean verifyUserInfo(String phoneNum, String email);
    boolean sendVerificationCode(String email);
    boolean verifyCode(String email, String code);
    boolean changePassword(String email, String newPassword);
    boolean sendSignupVerificationCode(String email);
    boolean verifySignupCode(String email, String code);
    
    UserDTO updateUserProfile(User user);
    
    UserDTO getMyInfo(UUID userId);
    UserDTO updateAssetInfo(UUID userId, Long salary, Long payAmount);
    UserDTO updateMbti(UUID userId, String mbti);
    boolean updatePassword(UUID userId, String newPassword);
    boolean updatePasswordWithCurrentCheck(UUID userId, String currentPassword, String newPassword);
    boolean withdrawUser(UUID userId);
}
