package org.ozea.product.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FssProductOptionDto {
    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd;
    
    @JsonProperty("intr_rate_type")
    private String intrRateType;
    
    @JsonProperty("intr_rate_type_nm")
    private String intrRateTypeNm;
    
    @JsonProperty("rsrv_type")
    private String rsrvType;
    
    @JsonProperty("rsrv_type_nm")
    private String rsrvTypeNm;
    
    @JsonProperty("save_trm")
    private Integer saveTrm;
    
    @JsonProperty("intr_rate")
    private Double intrRate;
    
    @JsonProperty("intr_rate2")
    private Double intrRate2;
    
    // 옵션 ID는 별도로 생성
    private Integer optionId;
} 