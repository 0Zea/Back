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
    private String sex;           // "female" or "male"
    private Long salary;          // 월급
    private Long payAmount;       // 월 지출비
    private String mbti;          // 금융 MBTI

    // DTO → VO
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
                .role("user") // 가입 시 기본 역할은 'user'
                .build();
    }
}
