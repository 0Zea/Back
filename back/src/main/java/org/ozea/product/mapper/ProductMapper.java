package org.ozea.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.ozea.product.dto.response.ProductResponseDto;

import java.util.List;

@Mapper
public interface ProductMapper {
    List<ProductResponseDto> findAllProductsWithOptions();
}
