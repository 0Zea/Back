package org.ozea.product.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ozea.product.client.dto.FssApiResponse;
import org.ozea.product.client.dto.FssProductDto;
import org.ozea.product.client.dto.FssProductOptionDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FssApiClient {

    private final RestTemplate restTemplate;

    @Value("${fss.api.key}")
    private String apiKey;

    @Value("${fss.api.base-url:http://finlife.fss.or.kr/finlifeapi}")
    private String baseUrl;

    /**
     * 정기예금 상품 목록 조회
     */
    public List<FssProductDto> getDepositProducts() {
        log.info("정기예금 상품 목록 조회 시작");
        
        List<FssProductDto> allProducts = new ArrayList<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            try {
                String url = String.format("%s/depositProductsSearch.json?auth=%s&topFinGrpNo=020000&pageNo=%d", 
                    baseUrl, apiKey, pageNo);
                
                ResponseEntity<FssApiResponse> response = restTemplate.getForEntity(url, FssApiResponse.class);
                
                if (response.getBody() != null && response.getBody().getResult() != null && 
                    response.getBody().getResult().getProducts() != null) {
                    List<FssProductDto> products = response.getBody().getResult().getProducts();
                    allProducts.addAll(products);
                    
                    // max_page_no를 사용해서 다음 페이지가 있는지 확인
                    Integer maxPageNo = response.getBody().getResult().getMaxPageNo();
                    if (maxPageNo != null && pageNo < maxPageNo) {
                        pageNo++;
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
                
                // API 호출 간격 조절 (초당 1회 제한)
                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("정기예금 상품 조회 중 오류 발생 (페이지: {})", pageNo, e);
                hasMore = false;
            }
        }
        
        log.info("정기예금 상품 조회 완료: {}개", allProducts.size());
        return allProducts;
    }

    /**
     * 적금 상품 목록 조회
     */
    public List<FssProductDto> getSavingProducts() {
        log.info("적금 상품 목록 조회 시작");
        
        List<FssProductDto> allProducts = new ArrayList<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            try {
                String url = String.format("%s/savingProductsSearch.json?auth=%s&topFinGrpNo=020000&pageNo=%d", 
                    baseUrl, apiKey, pageNo);
                
                ResponseEntity<FssApiResponse> response = restTemplate.getForEntity(url, FssApiResponse.class);
                
                if (response.getBody() != null && response.getBody().getResult() != null && 
                    response.getBody().getResult().getProducts() != null) {
                    List<FssProductDto> products = response.getBody().getResult().getProducts();
                    allProducts.addAll(products);
                    
                    // max_page_no를 사용해서 다음 페이지가 있는지 확인
                    Integer maxPageNo = response.getBody().getResult().getMaxPageNo();
                    if (maxPageNo != null && pageNo < maxPageNo) {
                        pageNo++;
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
                
                // API 호출 간격 조절
                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("적금 상품 조회 중 오류 발생 (페이지: {})", pageNo, e);
                hasMore = false;
            }
        }
        
        log.info("적금 상품 조회 완료: {}개", allProducts.size());
        return allProducts;
    }

    /**
     * 정기예금 상품 옵션 조회
     */
    public List<FssProductOptionDto> getDepositProductOptions() {
        log.info("정기예금 상품 옵션 조회 시작");
        
        List<FssProductOptionDto> allOptions = new ArrayList<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            try {
                String url = String.format("%s/depositProductsSearch.json?auth=%s&topFinGrpNo=020000&pageNo=%d", 
                    baseUrl, apiKey, pageNo);
                
                ResponseEntity<FssApiResponse> response = restTemplate.getForEntity(url, FssApiResponse.class);
                
                if (response.getBody() != null && response.getBody().getResult() != null && 
                    response.getBody().getResult().getOptions() != null) {
                    List<FssProductOptionDto> options = response.getBody().getResult().getOptions();
                    allOptions.addAll(options);
                    
                    // max_page_no를 사용해서 다음 페이지가 있는지 확인
                    Integer maxPageNo = response.getBody().getResult().getMaxPageNo();
                    if (maxPageNo != null && pageNo < maxPageNo) {
                        pageNo++;
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
                
                // API 호출 간격 조절
                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("정기예금 상품 옵션 조회 중 오류 발생 (페이지: {})", pageNo, e);
                hasMore = false;
            }
        }
        
        log.info("정기예금 상품 옵션 조회 완료: {}개", allOptions.size());
        return allOptions;
    }

    /**
     * 적금 상품 옵션 조회
     */
    public List<FssProductOptionDto> getSavingProductOptions() {
        log.info("적금 상품 옵션 조회 시작");
        
        List<FssProductOptionDto> allOptions = new ArrayList<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            try {
                String url = String.format("%s/savingProductsSearch.json?auth=%s&topFinGrpNo=020000&pageNo=%d", 
                    baseUrl, apiKey, pageNo);
                
                ResponseEntity<FssApiResponse> response = restTemplate.getForEntity(url, FssApiResponse.class);
                
                if (response.getBody() != null && response.getBody().getResult() != null && 
                    response.getBody().getResult().getOptions() != null) {
                    List<FssProductOptionDto> options = response.getBody().getResult().getOptions();
                    allOptions.addAll(options);
                    
                    // max_page_no를 사용해서 다음 페이지가 있는지 확인
                    Integer maxPageNo = response.getBody().getResult().getMaxPageNo();
                    if (maxPageNo != null && pageNo < maxPageNo) {
                        pageNo++;
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
                
                // API 호출 간격 조절
                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("적금 상품 옵션 조회 중 오류 발생 (페이지: {})", pageNo, e);
                hasMore = false;
            }
        }
        
        log.info("적금 상품 옵션 조회 완료: {}개", allOptions.size());
        return allOptions;
    }
} 