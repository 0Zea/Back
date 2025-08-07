package org.ozea.api.account.dto.request;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class AllAccountReqDto {
    private String countryCode;   // 국가코드
    private String businessType;  // 업무구분 (BK)
    private String clientType;    // 개인/기업 구분
    private String organization;  // 기관 코드 (예: 0004)
    private String loginType;     // 로그인 타입
    private String id;            // 계정 아이디
    private String password;      // 계정 비밀번호
}
