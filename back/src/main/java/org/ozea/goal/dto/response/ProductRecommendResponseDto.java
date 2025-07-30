package org.ozea.goal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class ProductRecommendResponseDto {
    private String finPrdtCd;       // 상품 코드
    private String korCoNm;         // 금융회사명
    private String finPrdtNm;       // 상품명
    private String rsrvTypeNm;      // 적립 유형
    private int saveTrm;            // 예치 기간
    private BigDecimal intrRate;    // 기본금리
    private BigDecimal intrRate2;   // 우대금리
}
