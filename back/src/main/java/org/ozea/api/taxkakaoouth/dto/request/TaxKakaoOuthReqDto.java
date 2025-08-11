package org.ozea.api.taxkakaoouth.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaxKakaoOuthReqDto {
    private String organization;
    private String loginType;
    private String identity;
    private String userName;
    private String phoneNo;
    private String loginTypeLevel;
    private String id;
    private String searchStartYear;
    private String inquiryTypeCD;
    private String telecom;
}
