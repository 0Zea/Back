package org.ozea.api.taxkakaoouth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaxOuthReqDto {
    private String organization;      // "0004"
    private String loginType;         // "1"
    private String simpleAuth;        // "1"
    private boolean Is2Way;           // true
    private String commSimpleAuth;    // ""
    private TwoWayInfoDto twoWayInfo; // 중첩 객체
}

