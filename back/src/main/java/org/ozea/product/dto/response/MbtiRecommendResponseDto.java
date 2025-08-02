package org.ozea.product.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MbtiRecommendResponseDto {
    private String bankName;
    private String productName;
    private Double intrRate;
    private Double intrRate2;
    private String reason;
}
