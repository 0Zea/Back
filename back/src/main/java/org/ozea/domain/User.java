package org.ozea.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 사용자 정보를 나타내는 도메인 클래스입니다.
 * Lombok의 @Data 어노테이션을 사용하여 getter, setter, toString, equals, hashCode 등을 자동으로 생성합니다.
 */
@Data
public class User {
    private UUID userId; // 사용자 고유 ID (UUID)
    private String name; // 사용자 이름
    private String email; // 사용자 이메일
    private String password; // 사용자 비밀번호
    private String mbti; // 사용자 MBTI
    private String phoneNum; // 사용자 전화번호
    private LocalDate birthDate; // 사용자 생년월일
    private String sex;       // 성별 (e.g., 'female', 'male')
    private Long salary; // 급여
    private Long payAmount; // 지불 금액
    private String role;      // 사용자 역할 (e.g., 'user', 'admin')

}
