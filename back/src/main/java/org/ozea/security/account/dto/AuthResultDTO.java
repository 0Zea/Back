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
    // 기존 호환성을 위한 생성자
    public AuthResultDTO(String token, UserInfoDTO user, boolean isNewUser) {
        this.accessToken = token;
        this.refreshToken = null;
        this.user = user;
        this.isNewUser = isNewUser;
    }
    // Refresh Token을 포함한 생성자
    public AuthResultDTO(String accessToken, String refreshToken, UserInfoDTO user, boolean isNewUser) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.isNewUser = isNewUser;
    }
}
