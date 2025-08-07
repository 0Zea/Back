package org.ozea.product.dto.response;
import lombok.*;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private String finPrdtCd;
    private String dclsMonth;
    private String finCoNo;
    private String korCoNm;
    private String finPrdtNm;
    private String joinWay;
    private String mtrtInt;
    private String spclCnd;
    private String joinDeny;
    private String joinMember;
    private String etcNote;
    private Long maxLimit;
    private String dclsStrtDay;
    private String dclsEndDay;
    private String finCoSubmDay;
    private List<ProductOptionDto> options;
}
