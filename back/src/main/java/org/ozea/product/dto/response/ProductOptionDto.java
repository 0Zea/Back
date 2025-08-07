package org.ozea.product.dto.response;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionDto {
    private Integer optionId;
    private String intrRateType;
    private String intrRateTypeNm;
    private String rsrvType;
    private String rsrvTypeNm;
    private Integer saveTrm;
    private Double intrRate;
    private Double intrRate2;
}
