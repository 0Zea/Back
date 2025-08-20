package org.ozea.api.taxkakaoouth.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoWayInfoDto {
    private int jobIndex;
    private int threadIndex;
    private String jti;
    private long twoWayTimestamp;
}