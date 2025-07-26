package org.ozea.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.user.domain.User;
import org.ozea.user.mapper.UserMapper;
import org.ozea.security.account.domain.CustomUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User vo = mapper.getUserByEmail(email);
        if(vo == null) {
            throw new UsernameNotFoundException(email + "은 없는 id입니다.");
        }
        return new CustomUser(vo);

    }

}
