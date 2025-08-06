package org.ozea.security.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
