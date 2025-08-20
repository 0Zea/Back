package org.ozea.taxinfo.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class TaxInfoItemDto {
    private String resDeductibleItem;
    private List<TaxBasicDto> resBasicList;
}
