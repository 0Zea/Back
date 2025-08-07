package org.ozea.product.dto.request;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Getter
@Setter
public class ProductFilterRequestDto {
    private List<String> bankNames;
    private List<String> joinMembers;
    private List<String> productType;
    private Integer minSaveTrm;
    private Integer maxSaveTrm;
    private Long minAmount;
    private Long maxAmount;
    private List<String> spclCndKeywords;
}
