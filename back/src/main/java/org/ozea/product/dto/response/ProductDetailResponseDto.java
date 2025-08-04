package org.ozea.product.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductDetailResponseDto {
    private String productName;
    private String bankName;
    private Long maxLimit;

    private String joinWay;        // 가입방법
    private String mtrtInt;        // 이자지급방식
    private String spclCnd;        // 우대조건
    private String joinMember;     // 가입대상
    private String etcNote;        // 유의사항

    private List<ProductOptionDto> options;
}

