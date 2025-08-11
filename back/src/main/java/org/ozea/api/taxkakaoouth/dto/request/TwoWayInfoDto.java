package org.ozea.api.taxkakaoouth.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoWayInfoDto {
    private int jobIndex;            // 0
    private int threadIndex;         // 0
    private String jti;              // Step1 응답에서 받은 값
    private long twoWayTimestamp;    // Step1 응답에서 받은 값
}