package org.ozea.api.allaccount.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AllAccountResDto {
    private String userId;
    private String countrycode;
    private String businesstype;
    private String clienttype;
    private String logintype;
    private String organization;
    private String accountId;
}
