package org.ozea.product.domain;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductOption {
    private Integer optionId;          // 옵션 ID (PK, Auto Increment)
    private String finPrdtCd;          // 금융상품 코드 (FK)
    private String intrRateType;       // 금리 유형 코드
    private String intrRateTypeNm;     // 금리 유형명
    private String rsrvType;           // 적립 유형 코드
    private String rsrvTypeNm;         // 적립 유형명 (예: 자유적립)
    private Integer saveTrm;           // 저축 기간 (단위: 개월)
    private Double intrRate;           // 기본 금리
    private Double intrRate2;          // 우대 금리
}
