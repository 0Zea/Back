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
    public User getUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }
    public UserDetails loadUserByUsername(String email, String nickname) throws UsernameNotFoundException {
        User user = userMapper.getUserByEmail(email);
        if (user == null) throw new UsernameNotFoundException("유저 없음");
        return new CustomUser(user, false);
    }
    public UserDetails loadUserByUsername(String email, String nickname, String accessToken, String refreshToken) throws UsernameNotFoundException {
        User user = userMapper.getUserByEmail(email);
        boolean isNewUser = false;
        if (user == null) {
            isNewUser = true;
            user = new User();
            user.setUserId(UUID.randomUUID());
            user.setEmail(email);
            if (nickname == null || nickname.isEmpty()) {
                nickname = "카카오사용자";
            }
            user.setName(nickname);
            user.setMbti("미입력");
            user.setPhoneNum("000-0000-0000");
            user.setBirthDate(java.time.LocalDate.now());
            user.setSex("female");
            user.setSalary(0L);
            user.setPayAmount(0L);
            user.setRole("USER");
            user.setKakaoAccessToken(accessToken);
            // 신규 사용자 등록을 지연시킴 - DB 삽입하지 않음
        } else {
            user.setKakaoAccessToken(accessToken);
            userMapper.updateUser(user);
        }
        return new CustomUser(user, isNewUser);
    }
    
    // 신규 사용자를 DB에 등록하는 별도 메서드
    public void registerNewUser(User user) {
        userMapper.insertUserWithEmail(user);
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return loadUserByUsername(email, null);
    }
}