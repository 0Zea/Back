package org.ozea.product.scheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ozea.product.client.FssApiClient;
import org.ozea.product.client.dto.FssProductDto;
import org.ozea.product.client.dto.FssProductOptionDto;
import org.ozea.product.domain.Product;
import org.ozea.product.mapper.ProductMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductScheduler {
    private final FssApiClient fssApiClient;
    private final ProductMapper productMapper;
    /**
     * 매일 새벽 2시에 금융감독원 API에서 상품 정보를 가져와서 DB에 저장
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateProductsFromFss() {
        updateProductsFromFssInternal();
    }
    /**
     * 수동으로 상품 정보 업데이트 (테스트용)
     */
    public void updateProductsFromFssManual() {
        updateProductsFromFssInternal();
    }
    /**
     * 테스트용 더미 상품 데이터 생성
     */
    public void createDummyProducts() {
        log.info("더미 상품 데이터 생성 시작");
        try {
            // 기존 상품 옵션 데이터 삭제
            productMapper.deleteAllProductOptions();
            log.info("기존 상품 옵션 데이터 삭제 완료");
            // 기존 상품 데이터 삭제
            productMapper.deleteAllProducts();
            log.info("기존 상품 데이터 삭제 완료");
            // 더미 예금 상품 생성
            Product dummyDeposit1 = Product.builder()
                    .finPrdtCd("DEP001")
                    .dclsMonth("202408")
                    .finCoNo("001")
                    .korCoNm("국민은행")
                    .finPrdtNm("국민 정기예금")
                    .joinWay("영업점")
                    .mtrtInt("연 3.5%")
                    .spclCnd("신규고객 우대")
                    .joinDeny("1")
                    .joinMember("개인")
                    .etcNote("만기 후 자동재예치")
                    .maxLimit(10000000L)
                    .dclsStrtDay("20240801")
                    .dclsEndDay("20241231")
                    .finCoSubmDay(null)
                    .build();
            Product dummyDeposit2 = Product.builder()
                    .finPrdtCd("DEP002")
                    .dclsMonth("202408")
                    .finCoNo("002")
                    .korCoNm("신한은행")
                    .finPrdtNm("신한 스마트예금")
                    .joinWay("인터넷")
                    .mtrtInt("연 3.8%")
                    .spclCnd("온라인 신규")
                    .joinDeny("1")
                    .joinMember("개인")
                    .etcNote("온라인 전용")
                    .maxLimit(50000000L)
                    .dclsStrtDay("20240801")
                    .dclsEndDay("20241231")
                    .finCoSubmDay(null)
                    .build();
            // 더미 적금 상품 생성
            Product dummySaving1 = Product.builder()
                    .finPrdtCd("SAV001")
                    .dclsMonth("202408")
                    .finCoNo("003")
                    .korCoNm("우리은행")
                    .finPrdtNm("우리 자유적금")
                    .joinWay("영업점")
                    .mtrtInt("연 4.0%")
                    .spclCnd("자유입금")
                    .joinDeny("1")
                    .joinMember("개인")
                    .etcNote("자유입금 가능")
                    .maxLimit(30000000L)
                    .dclsStrtDay("20240801")
                    .dclsEndDay("20241231")
                    .finCoSubmDay(null)
                    .build();
            Product dummySaving2 = Product.builder()
                    .finPrdtCd("SAV002")
                    .dclsMonth("202408")
                    .finCoNo("004")
                    .korCoNm("하나은행")
                    .finPrdtNm("하나 정기적금")
                    .joinWay("영업점")
                    .mtrtInt("연 4.5%")
                    .spclCnd("정기입금")
                    .joinDeny("1")
                    .joinMember("개인")
                    .etcNote("정기입금 필수")
                    .maxLimit(20000000L)
                    .dclsStrtDay("20240801")
                    .dclsEndDay("20241231")
                    .finCoSubmDay(null)
                    .build();
            // 상품 데이터 저장
            productMapper.insertProduct(dummyDeposit1);
            productMapper.insertProduct(dummyDeposit2);
            productMapper.insertProduct(dummySaving1);
            productMapper.insertProduct(dummySaving2);
            // 상품 옵션 데이터 저장
            productMapper.insertProductOption("DEP001", 1, "S", "단리", "자유적립식", "자유적립식", 12, 3.5, 4.0);
            productMapper.insertProductOption("DEP001", 2, "S", "단리", "정기적립식", "정기적립식", 24, 3.8, 4.2);
            productMapper.insertProductOption("DEP002", 3, "S", "단리", "자유적립식", "자유적립식", 12, 3.8, 4.1);
            productMapper.insertProductOption("DEP002", 4, "S", "단리", "정기적립식", "정기적립식", 36, 4.0, 4.3);
            productMapper.insertProductOption("SAV001", 5, "S", "단리", "자유적립식", "자유적립식", 12, 4.0, 4.5);
            productMapper.insertProductOption("SAV001", 6, "S", "단리", "정기적립식", "정기적립식", 24, 4.2, 4.7);
            productMapper.insertProductOption("SAV002", 7, "S", "단리", "자유적립식", "자유적립식", 12, 4.5, 5.0);
            productMapper.insertProductOption("SAV002", 8, "S", "단리", "정기적립식", "정기적립식", 36, 4.5, 5.0);
            log.info("더미 상품 데이터 생성 완료: {}개 상품, {}개 옵션", 4, 8);
        } catch (Exception e) {
            log.error("더미 상품 데이터 생성 중 오류 발생", e);
            throw e;
        }
    }
    /**
     * 실제 상품 정보 업데이트 로직
     */
    private void updateProductsFromFssInternal() {
        log.info("금융감독원 API에서 상품 정보 업데이트 시작");
        try {
            // 기존 상품 옵션 데이터 삭제 (외래키 제약조건 때문에 먼저 삭제)
            productMapper.deleteAllProductOptions();
            log.info("기존 상품 옵션 데이터 삭제 완료");
            // 기존 상품 데이터 삭제
            productMapper.deleteAllProducts();
            log.info("기존 상품 데이터 삭제 완료");
            // 예금 상품 가져오기
            List<FssProductDto> depositProducts = fssApiClient.getDepositProducts();
            log.info("예금 상품 {}개 가져옴", depositProducts.size());
            // 적금 상품 가져오기
            List<FssProductDto> savingProducts = fssApiClient.getSavingProducts();
            log.info("적금 상품 {}개 가져옴", savingProducts.size());
            // 예금 상품 옵션 가져오기
            List<FssProductOptionDto> depositOptions = fssApiClient.getDepositProductOptions();
            log.info("예금 상품 옵션 {}개 가져옴", depositOptions.size());
            // 적금 상품 옵션 가져오기
            List<FssProductOptionDto> savingOptions = fssApiClient.getSavingProductOptions();
            log.info("적금 상품 옵션 {}개 가져옴", savingOptions.size());
            // 모든 상품 합치기
            List<FssProductDto> allProducts = new ArrayList<>();
            allProducts.addAll(depositProducts);
            allProducts.addAll(savingProducts);
            // 모든 옵션 합치기
            List<FssProductOptionDto> allOptions = new ArrayList<>();
            allOptions.addAll(depositOptions);
            allOptions.addAll(savingOptions);
            log.info("총 {}개 상품, {}개 옵션 처리 시작", allProducts.size(), allOptions.size());
            // 상품 데이터 저장
            for (FssProductDto fssProduct : allProducts) {
                try {
                    Product product = convertToProduct(fssProduct);
                    productMapper.insertProduct(product);
                    log.debug("상품 저장 완료: {}", fssProduct.getFinPrdtCd());
                } catch (Exception e) {
                    log.error("상품 저장 중 오류 발생: {} - {}", fssProduct.getFinPrdtCd(), e.getMessage());
                }
            }
            // 옵션 데이터 저장
            int optionIdCounter = 1;
            for (FssProductOptionDto fssOption : allOptions) {
                try {
                    productMapper.insertProductOption(
                        fssOption.getFinPrdtCd(),
                        optionIdCounter++, // 자동 증가하는 옵션 ID
                        fssOption.getIntrRateType(),
                        fssOption.getIntrRateTypeNm(),
                        fssOption.getRsrvType(),
                        fssOption.getRsrvTypeNm(),
                        fssOption.getSaveTrm(),
                        fssOption.getIntrRate(),
                        fssOption.getIntrRate2()
                    );
                    log.debug("옵션 저장 완료: {} - {}", fssOption.getFinPrdtCd(), optionIdCounter - 1);
                } catch (Exception e) {
                    log.error("옵션 저장 중 오류 발생: {} - {} - {}", fssOption.getFinPrdtCd(), optionIdCounter - 1, e.getMessage());
                }
            }
            log.info("상품 정보 업데이트 완료: {}개 상품, {}개 옵션 저장됨", allProducts.size(), allOptions.size());
        } catch (Exception e) {
            log.error("상품 정보 업데이트 중 오류 발생", e);
            throw e;
        }
    }
    /**
     * FssProductDto를 Product 엔티티로 변환
     */
    private Product convertToProduct(FssProductDto fssProduct) {
        return Product.builder()
                .finPrdtCd(fssProduct.getFinPrdtCd())
                .dclsMonth(fssProduct.getDclsMonth())
                .finCoNo(fssProduct.getFinCoNo())
                .korCoNm(fssProduct.getKorCoNm())
                .finPrdtNm(fssProduct.getFinPrdtNm())
                .joinWay(fssProduct.getJoinWay())
                .mtrtInt(fssProduct.getMtrtInt())
                .spclCnd(fssProduct.getSpclCnd())
                .joinDeny(fssProduct.getJoinDeny())
                .joinMember(fssProduct.getJoinMember())
                .etcNote(fssProduct.getEtcNote())
                .maxLimit(fssProduct.getMaxLimit())
                .dclsStrtDay(fssProduct.getDclsStrtDay())
                .dclsEndDay(fssProduct.getDclsEndDay())
                .finCoSubmDay(fssProduct.getFinCoSubmDay())
                .build();
    }
}