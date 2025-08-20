package org.ozea.security.config;

import org.ozea.user.domain.User;
import org.ozea.user.mapper.UserMapper;
import org.ozea.security.account.domain.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class KakaoUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    public CustomUser loadKakaoUser(String email, String nickname) {
        User user = userMapper.getUserByEmail(email);
        boolean isNewUser = false;
        if (user == null) {
            isNewUser = true;
            user = new User();
            user.setEmail(email);
            user.setName(nickname != null && !nickname.isEmpty() ? nickname : "카카오사용자");
        }
        return new CustomUser(user, isNewUser);
    }

    public User getUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userMapper.getUserByEmail(email);
        if (user == null) throw new UsernameNotFoundException("유저 없음");
        return new CustomUser(user, false);
    }

    @Transactional
    public UserDetails loadUserByUsername(String email, String nickname, String accessToken, String refreshToken)
            throws UsernameNotFoundException {

        User user = userMapper.getUserByEmail(email);
        boolean isNewUser = false;

        if (user == null) {
            isNewUser = true;
            user = new User();
            user.setUserId(UUID.randomUUID());
            user.setEmail(email);
            user.setName(nickname != null && !nickname.isEmpty() ? nickname : "카카오사용자");
            user.setMbti("미입력");
            user.setPhoneNum("000-0000-0000");
            user.setBirthDate(java.time.LocalDate.now());
            user.setSex("female");
            user.setSalary(0L);
            user.setPayAmount(0L);
            user.setRole("USER");
            user.setKakaoAccessToken(accessToken);
        } else {
            user.setKakaoAccessToken(accessToken);
            userMapper.updateKakaoAccessToken(email, accessToken);
        }

        return new CustomUser(user, isNewUser);
    }

    @Transactional
    public void registerNewUser(User user) {
        if (user.getRole() == null) user.setRole("USER");
        if (user.getPassword() == null) user.setPassword("");
        userMapper.insertUserWithEmail(user);
    }
}