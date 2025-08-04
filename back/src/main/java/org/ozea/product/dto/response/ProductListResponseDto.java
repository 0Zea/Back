package org.ozea.product.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductListResponseDto {
    private String finPrdtCd;
    private String productName;
    private String bankName;
    private double intrRate;
    private double intrRate2;
}
