package org.ozea.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User  {
    private UUID userId;
    private String name;
    private String email;
    private String password;
    private String mbti;
    private String phoneNum;
    private LocalDate birthDate;
    private String sex;
    private Long salary;
    private Long payAmount;
    private String role;
    private String kakaoAccessToken;
} 