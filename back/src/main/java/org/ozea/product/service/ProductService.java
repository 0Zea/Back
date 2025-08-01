package org.ozea.product.service;

import org.ozea.product.dto.response.MbtiRecommendResponseDto;
import org.ozea.product.dto.response.ProductResponseDto;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<ProductResponseDto> getAllProductsWithOptions();
    List<MbtiRecommendResponseDto> getRecommendedProductsByMbti(UUID userId);
}
