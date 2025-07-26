package org.ozea.security.account.domain;

import lombok.Getter;
import lombok.Setter;
import org.ozea.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
public class CustomUser implements UserDetails {
    private final User user;
    private final boolean isNewUser;

    public CustomUser(User user, boolean isNewUser) {
        this.user = user;
        this.isNewUser = isNewUser;
    }

    public CustomUser(User user) {
        this(user, false);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = user.getRole();
        if (role == null) {
            role = "USER"; 
        }
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
