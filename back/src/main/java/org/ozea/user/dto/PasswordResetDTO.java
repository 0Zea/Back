package org.ozea.user.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDTO {
    private String phoneNum;
    private String email;
    private String code;
    private String newPassword;
}