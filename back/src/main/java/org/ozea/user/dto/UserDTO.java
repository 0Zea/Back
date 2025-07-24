package org.ozea.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ozea.domain.User;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String userId;
    private String name;
    private String email;
    private String mbti;
    private String phoneNum;
    private LocalDate birthDate;
    private String sex;
    private Long salary;
    private Long payAmount;
    private String role;

    // VO -> DTO
    public static UserDTO of(User user){
        return UserDTO.builder()
                .userId(user.getUserId().toString())
                .name(user.getName())
                .email(user.getEmail())
                .mbti(user.getMbti())
                .phoneNum(user.getPhoneNum())
                .birthDate(user.getBirthDate())
                .sex(user.getSex())
                .salary(user.getSalary())
                .payAmount(user.getPayAmount())
                .role(user.getRole())
                .build();
    }

    // DTO -> VO
    public User toVO(){
        return User.builder()
                .userId(UUID.fromString(userId))
                .name(name)
                .email(email)
                .mbti(mbti)
                .phoneNum(phoneNum)
                .birthDate(birthDate)
                .sex(sex)
                .salary(salary)
                .payAmount(payAmount)
                .role(role)
                .build();
    }
}
