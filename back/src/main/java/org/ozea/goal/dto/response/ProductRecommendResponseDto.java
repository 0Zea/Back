package org.ozea.goal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class ProductRecommendResponseDto {
    private String finPrdtCd;
    private String korCoNm;
    private String finPrdtNm;
    private String rsrvTypeNm;
    private int saveTrm;
    private BigDecimal intrRate;
    private BigDecimal intrRate2;
}
