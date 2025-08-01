package org.ozea.product.controller;

import lombok.RequiredArgsConstructor;
import org.ozea.product.dto.response.MbtiRecommendResponseDto;
import org.ozea.product.dto.response.ProductResponseDto;
import org.ozea.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProductsWithOptions());
    }

    @GetMapping("/recommend")
    public List<MbtiRecommendResponseDto> getRecommended(@RequestParam UUID userId) {
        return productService.getRecommendedProductsByMbti(userId);
    }

}
