package org.ozea.user.service;

import org.ozea.domain.User;
import org.ozea.user.dto.UserDTO;
import org.ozea.user.dto.UserSignupDTO;

public interface UserService {
    boolean checkEmail(String email);       // 이메일 중복 확인
    UserDTO signup(UserSignupDTO dto);      // 회원가입
    UserDTO getUserByEmail(String email);   // 이메일을 기준으로 사용자 정보를 조회
}
