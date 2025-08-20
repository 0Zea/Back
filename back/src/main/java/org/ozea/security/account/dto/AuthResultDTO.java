package org.ozea.security.account.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class AuthResultDTO {
    private String accessToken;
    private String refreshToken;
    private UserInfoDTO user;
    private boolean isNewUser = false;
    public AuthResultDTO(String token, UserInfoDTO user, boolean isNewUser) {
        this.accessToken = token;
        this.refreshToken = null;
        this.user = user;
        this.isNewUser = isNewUser;
    }
    public AuthResultDTO(String accessToken, String refreshToken, UserInfoDTO user, boolean isNewUser) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.isNewUser = isNewUser;
    }
}
