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
    private String joinWay;
    private String mtrtInt;
    private String spclCnd;
    private String joinMember;
    private String etcNote;
    private String summary;
    private List<ProductOptionDto> options;
}
