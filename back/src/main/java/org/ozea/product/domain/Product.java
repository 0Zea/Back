package org.ozea.product.domain;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private String finPrdtCd;        // 금융상품 코드 (PK)
    private String dclsMonth;        // 공시 제출월
    private String finCoNo;          // 금융회사 코드
    private String korCoNm;          // 금융회사명
    private String finPrdtNm;        // 금융상품명
    private String joinWay;          // 가입 방법
    private String mtrtInt;          // 만기 후 이자율
    private String spclCnd;          // 우대 조건
    private String joinDeny;         // 가입 제한
    private String joinMember;       // 가입 대상
    private String etcNote;          // 기타 유의사항
    private Long maxLimit;           // 최대한도
    private String dclsStrtDay;      // 공시 시작일
    private String dclsEndDay;       // 공시 종료일
    private String finCoSubmDay;     // 금융회사 제출일
}
