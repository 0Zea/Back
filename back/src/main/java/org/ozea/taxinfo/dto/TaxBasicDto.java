package org.ozea.taxinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)

public class TaxBasicDto {
    private String resUserNm;
    private String resUserIdentiyNo;
    private String resType;
    private String resCompanyNm;
    private String resCompanyIdentityNo;
    private String resAccount;
    private String resInsureType;
    private String resEduCostDetail;
    private String resAmount;
    private String resAmount1;
    private String resAmountPayment;
    private String resAmountPayment1;
    private List<TaxDetailDto> resDetailList;
    private Integer basicId;

    @Setter
    @Getter
    private String resDeductibleItem;

}
