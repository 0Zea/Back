package org.ozea.security.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ozea.user.domain.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    String username;
    String email;
    String role;

    public static UserInfoDTO of(User user) {
        return new UserInfoDTO(
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
