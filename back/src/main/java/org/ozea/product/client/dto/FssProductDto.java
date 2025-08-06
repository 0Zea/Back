package org.ozea.product.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FssProductDto {
    @JsonProperty("dcls_month")
    private String dclsMonth;
    
    @JsonProperty("fin_co_no")
    private String finCoNo;
    
    @JsonProperty("kor_co_nm")
    private String korCoNm;
    
    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd;
    
    @JsonProperty("fin_prdt_nm")
    private String finPrdtNm;
    
    @JsonProperty("join_way")
    private String joinWay;
    
    @JsonProperty("mtrt_int")
    private String mtrtInt;
    
    @JsonProperty("spcl_cnd")
    private String spclCnd;
    
    @JsonProperty("join_deny")
    private String joinDeny;
    
    @JsonProperty("join_member")
    private String joinMember;
    
    @JsonProperty("etc_note")
    private String etcNote;
    
    @JsonProperty("max_limit")
    private Long maxLimit;
    
    @JsonProperty("dcls_strt_day")
    private String dclsStrtDay;
    
    @JsonProperty("dcls_end_day")
    private String dclsEndDay;
    
    @JsonProperty("fin_co_subm_day")
    private String finCoSubmDay;
    
    private List<FssProductOptionDto> options;
} 