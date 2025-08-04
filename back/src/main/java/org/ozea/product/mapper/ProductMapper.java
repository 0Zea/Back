package org.ozea.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ozea.product.dto.request.ProductFilterRequestDto;
import org.ozea.product.dto.response.ProductDetailResponseDto;
import org.ozea.product.dto.response.ProductListResponseDto;
import org.ozea.product.dto.response.ProductOptionDto;
import org.ozea.product.dto.response.ProductResponseDto;

import java.util.List;

@Mapper
public interface ProductMapper {
    List<ProductResponseDto> findAllProductsWithOptions();
    List<ProductListResponseDto> getProducts(@Param("offset") int offset, @Param("size") int size);
    int countAllProducts();
    ProductDetailResponseDto getProductDetail(@Param("finPrdtCd") String finPrdtCd);
    List<ProductOptionDto> getProductOptions(@Param("finPrdtCd") String finPrdtCd);
    List<ProductListResponseDto> filterProducts(@Param("filter") ProductFilterRequestDto filterDto);
}
