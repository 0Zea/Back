package org.ozea.security.config;

import org.ozea.user.domain.User;
import org.ozea.user.mapper.UserMapper;
import org.ozea.security.account.domain.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Spring Security의 UserDetailsService를 구현한 클래스입니다.
 * 카카오 로그인 시 사용자 정보를 처리합니다.
 */
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
            user.setName(nickname != null ? nickname : "카카오사용자");
        }

        return new CustomUser(user, isNewUser); // DB insert 제거
    }

    /**
     * 외부에서 이메일로 User를 조회할 수 있도록 public 메서드 제공
     */
    public User getUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }

    /**
     * 이메일을 사용하여 사용자 정보를 조회합니다.
     * 만약 사용자가 존재하지 않으면 새로운 사용자를 생성하여 데이터베이스에 저장합니다.
     * @param email 사용자 이메일 (username으로 사용)
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때 발생하는 예외
     */
    // nickname을 받아 name에 저장하는 오버로드 메서드
    public UserDetails loadUserByUsername(String email, String nickname) throws UsernameNotFoundException {
//        User user = userMapper.getUserByEmail(email);
//        boolean isNewUser = false;
//        if (user == null) {
//            isNewUser = true;
//            user = new User();
//            user.setUserId(UUID.randomUUID());
//            user.setEmail(email);
//            if (nickname == null || nickname.isEmpty()) {
//                nickname = "카카오사용자";
//            }
//            user.setName(nickname); // name만 저장
//            // 나머지 필드는 null로 저장
//            user.setMbti("xxxx");
//            user.setPhoneNum("000-0000-0000");
//            user.setBirthDate(java.time.LocalDate.now());
//            user.setSex("female");
//            user.setSalary(0L);
//            user.setPayAmount(0L);
//            user.setRole("USER");
//            userMapper.insertUserWithEmail(user);
//        }
//        return new CustomUser(user, isNewUser);

        User user = userMapper.getUserByEmail(email);
        if (user == null) throw new UsernameNotFoundException("유저 없음");
        return new CustomUser(user, false);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return loadUserByUsername(email, null);
    }
}