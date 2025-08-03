package org.ozea.product.domain;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductOption {
    private Integer optionId;
    private String finPrdtCd;
    private String intrRateType;
    private String intrRateTypeNm;
    private String rsrvType;
    private String rsrvTypeNm;
    private Integer saveTrm;
    private Double intrRate;
    private Double intrRate2;
}
