package org.ozea.user.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ozea.user.domain.User;
import java.time.LocalDate;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupDTO {
    private String name;
    private String email;
    private String password;
    private String phoneNum;
    private LocalDate birthDate;
    private String sex;
    private Long salary;
    private Long payAmount;
    private String mbti;
    private boolean kakao;
    public User toVO() {
        return User.builder()
                .userId(UUID.randomUUID())
                .name(name)
                .email(email)
                .password(password)
                .mbti(mbti)
                .phoneNum(phoneNum)
                .birthDate(birthDate)
                .sex(sex)
                .salary(salary)
                .payAmount(payAmount)
                .role("user")
                .build();
    }
}
