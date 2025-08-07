package org.ozea.security.account.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ozea.user.domain.User;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    String userId;
    String username;
    String email;
    String role;
    public static UserInfoDTO of(User user) {
        return new UserInfoDTO(
                user.getUserId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}