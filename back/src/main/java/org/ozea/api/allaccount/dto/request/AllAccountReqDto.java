package org.ozea.api.allaccount.dto.request;


import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class AllAccountReqDto {
    private String countryCode;
    private String businessType;
    private String clientType;
    private String organization;
    private String loginType;
    private String id;
    private String password;
}
