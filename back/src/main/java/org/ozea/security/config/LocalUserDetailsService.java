package org.ozea.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.domain.User;
import org.ozea.mapper.UserMapper;
import org.ozea.security.account.domain.CustomUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Log4j2
@Service
@RequiredArgsConstructor
public class LocalUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("🔍 로컬 로그인 요청된 사용자 이메일: {}", email);

        User user = userMapper.getUserByEmail(email);

        if (user == null) {
            log.error("❌ 사용자 정보 없음: {}", email);
            throw new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다.");
        }

        log.info("✅ 사용자 정보 조회 성공: {}", user.getEmail());

        return new CustomUser(user);
    }
}
