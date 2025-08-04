package org.ozea.product.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductListRequestDto {
    private int page = 1;
    private int size = 8;
}
