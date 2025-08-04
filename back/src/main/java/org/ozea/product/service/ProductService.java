package org.ozea.product.service;

import org.ozea.product.dto.request.ProductFilterRequestDto;
import org.ozea.product.dto.response.MbtiRecommendResponseDto;
import org.ozea.product.dto.response.ProductDetailResponseDto;
import org.ozea.product.dto.response.ProductListResponseDto;
import org.ozea.product.dto.response.ProductResponseDto;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<MbtiRecommendResponseDto> getRecommendedProductsByMbti(UUID userId);
    List<ProductListResponseDto> getProductList(int page, int size);
    int getTotalProductCount();
    ProductDetailResponseDto getProductDetail(String finPrdtCd);
    List<ProductListResponseDto> filterProducts(ProductFilterRequestDto filterDto);

}
