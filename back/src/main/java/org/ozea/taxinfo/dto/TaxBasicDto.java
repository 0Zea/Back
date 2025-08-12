package org.ozea.taxinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)  // ← 여기를 추가

public class TaxBasicDto {
    private String resUserNm;
    private String resUserIdentiyNo;
    private String resType;
    private String resCompanyNm;
    private String resCompanyIdentityNo;   // ← JSON 필드 추가
    private String resAccount;         // ← 여기 추가
    private String resInsureType;
    private String resEduCostDetail;
    private String resAmount;
    private String resAmount1;
    private String resAmountPayment;
    private String resAmountPayment1;
    private List<TaxDetailDto> resDetailList;
    private Integer basicId;  // MyBatis generated key

    // 추가한 필드의 getter/setter
    @Setter
    @Getter
    private String resDeductibleItem;   // ← 추가

    // getters / setters

}
