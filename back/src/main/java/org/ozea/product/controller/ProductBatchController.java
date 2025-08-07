package org.ozea.product.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ozea.product.scheduler.ProductScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/api/product/batch")
@RequiredArgsConstructor
public class ProductBatchController {
    private final ProductScheduler productScheduler;
    /**
     * 금융감독원 API에서 상품 정보를 가져와서 DB에 저장 (수동 실행)
     */
    @PostMapping("/update-from-fss")
    public ResponseEntity<String> updateProductsFromFss() {
        try {
            log.info("수동 상품 정보 업데이트 시작");
            productScheduler.updateProductsFromFssManual();
            log.info("수동 상품 정보 업데이트 완료");
            return ResponseEntity.ok("상품 정보 업데이트가 완료되었습니다.");
        } catch (Exception e) {
            log.error("수동 상품 정보 업데이트 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("상품 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    /**
     * 더미 상품 데이터 생성 (테스트용)
     */
    @PostMapping("/create-dummy")
    public ResponseEntity<String> createDummyProducts() {
        try {
            log.info("더미 상품 데이터 생성 시작");
            productScheduler.createDummyProducts();
            log.info("더미 상품 데이터 생성 완료");
            return ResponseEntity.ok("더미 상품 데이터 생성이 완료되었습니다.");
        } catch (Exception e) {
            log.error("더미 상품 데이터 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("더미 상품 데이터 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}