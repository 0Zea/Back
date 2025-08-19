package org.ozea.api.taxkakaoouth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaxOuthReqDto {
    private String organization;
    private String loginType;
    private String simpleAuth;
    private boolean Is2Way;
    private String commSimpleAuth;
    private TwoWayInfoDto twoWayInfo;
}

