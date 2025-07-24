package org.ozea.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.domain.User;
import org.ozea.mapper.UserMapper;
import org.ozea.user.dto.UserDTO;
import org.ozea.user.dto.UserSignupDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    final UserMapper mapper;
    final PasswordEncoder passwordEncoder;

    // 이메일 중복 확인
    @Override
    public boolean checkEmail(String email) {
        return mapper.checkEmail(email);
    }

    // 회원가입
    @Transactional
    @Override
    public UserDTO signup(UserSignupDTO dto) {
        // DTO → VO
        User user = dto.toVO();

        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // DB 저장
        mapper.insertUser(user);

        // 저장된 사용자 정보 조회 후 DTO 반환
        return getUserByEmail(user.getEmail());
    }

    // 이메일을 기준으로 사용자 정보를 조회
    @Override
    public UserDTO getUserByEmail(String email) {
        User user = mapper.getUserByEmail(email);
        return UserDTO.of(user); // DTO 변환 메서드 필요
    }

}
