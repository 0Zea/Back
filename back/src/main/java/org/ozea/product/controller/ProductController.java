package org.ozea.product.controller;
import lombok.RequiredArgsConstructor;
import org.ozea.product.dto.request.ProductFilterRequestDto;
import org.ozea.product.dto.response.MbtiRecommendResponseDto;
import org.ozea.product.dto.response.ProductDetailResponseDto;
import org.ozea.product.dto.response.ProductListResponseDto;
import org.ozea.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    @GetMapping
    public ResponseEntity<?> getProductList(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "8") int size) {
        List<ProductListResponseDto> products = productService.getProductList(page, size);
        int totalCount = productService.getTotalProductCount();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("products", products);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/{finPrdtCd}")
    public ResponseEntity<ProductDetailResponseDto> getProductDetail(@PathVariable String finPrdtCd) {
        return ResponseEntity.ok(productService.getProductDetail(finPrdtCd));
    }
    @GetMapping("/recommend")
    public ResponseEntity<?> getRecommended(@RequestParam UUID userId) {
        try {
            List<MbtiRecommendResponseDto> recommendations = productService.getRecommendedProductsByMbti(userId);
            return ResponseEntity.ok(recommendations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "추천 상품 조회 중 오류가 발생했습니다."));
        }
    }
    @PostMapping("/filter")
    public ResponseEntity<List<ProductListResponseDto>> filterProducts(
            @RequestBody ProductFilterRequestDto filterDto) {
        List<ProductListResponseDto> products = productService.filterProducts(filterDto);
        return ResponseEntity.ok(products);
    }
}
