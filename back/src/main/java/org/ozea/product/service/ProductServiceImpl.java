package org.ozea.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ozea.common.cache.CacheHelper;
import org.ozea.product.dto.request.ProductFilterRequestDto;
import org.ozea.product.dto.response.*;
import org.ozea.product.mapper.ProductMapper;
import org.ozea.user.domain.User;
import org.ozea.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newFixedThreadPool;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final org.ozea.ai.service.OpenAISummarizeService summarizeService;
    private final Executor executor = newFixedThreadPool(2);
    private final RedisTemplate<String, Object> redis;
    private final CacheHelper cacheHelper;
    @Autowired
    private StringRedisTemplate srt;

    @Cacheable(value = "product:list", key = "'p='+#page+', s ='+#size")
    @Override
    public List<ProductListResponseDto> getProductList(int page, int size) {
        int offset = (page - 1) * size;
        return productMapper.getProducts(offset, size);
    }

    @Cacheable(value = "product:detail", key = "#finPrdtCd")
    @Override
    public ProductDetailResponseDto getProductDetail(String finPrdtCd) {
        ProductDetailResponseDto detail = productMapper.getProductDetail(finPrdtCd);
        if (detail == null) throw new IllegalArgumentException("해당 상품이 존재하지 않습니다: " + finPrdtCd);

        List<ProductOptionDto> options = productMapper.getProductOptions(finPrdtCd);
        detail.setOptions(options);
        enqueueSummaryIfNeeded(finPrdtCd,detail);
        if (detail.getSummary() == null || detail.getSummary().trim().isEmpty()) {
            tryGenerateSummaryOnce(finPrdtCd, detail);
        }
        return detail;
    }

    @Override
    public int getTotalProductCount() {
        return productMapper.countAllProducts();
    }

    @Cacheable(
            value = "product:filter",
            key = "T(java.util.Objects).hash(#filterDto)")
    @Override
    public List<ProductListResponseDto> filterProducts(ProductFilterRequestDto filterDto) {
        return productMapper.filterProducts(filterDto);
    }

    @Override
    public List<MbtiRecommendResponseDto> getRecommendedProductsByMbti(UUID userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        if (user.getMbti() == null || user.getMbti().trim().isEmpty() || "미입력".equals(user.getMbti())) {
            // MBTI가 없거나 미입력인 경우 기본 추천 상품 반환
            List<ProductResponseDto> products = productMapper.findAllProductsWithOptions();
            return products.stream()
                    .sorted(rate2Desc())
                    .limit(6)
                    .map(p -> toDto(p, "MBTI 정보 없음 - 기본 추천 상품"))
                    .toList();
        }
        String mbti = user.getMbti();
        List<ProductResponseDto> products = productMapper.findAllProductsWithOptions();
        return switch (mbti) {
            case "신속한 승부사" -> products.stream()
                    .filter(p -> hasShortTerm(p, 6) && isFreeSaving(p) && isHighRate(p))
                    .sorted(rate2Desc())
                    .limit(6)
                    .map(p -> toDto(p, "단기 + 자유 적립 고금리 상품"))
                    .toList();
            case "신중한 승부사" -> products.stream()
                    .filter(p -> hasLongTerm(p, 12) && isBigBank(p.getKorCoNm()))
                    .sorted(avgIntrRateDesc())
                    .limit(6)
                    .map(p -> toDto(p, "안정적이고 장기적인 상품"))
                    .toList();
            case "신속한 분석가" -> products.stream()
                    .filter(p -> hasShortTerm(p, 6))
                    .sorted(rateDesc())
                    .limit(6)
                    .map(p -> toDto(p, "짧고 빠르게 회전 가능한 상품"))
                    .toList();
            case "신중한 분석가" -> products.stream()
                    .filter(p -> hasLongTerm(p, 12))
                    .sorted(rate2Desc())
                    .limit(6)
                    .map(p -> toDto(p, "장기 분석 기반 추천"))
                    .toList();
            default -> {
                // 알 수 없는 MBTI인 경우 기본 추천 상품 반환
                yield products.stream()
                        .sorted(rate2Desc())
                        .limit(6)
                        .map(p -> toDto(p, "기본 추천 상품"))
                        .toList();
            }
        };
    }

    private boolean isFreeSaving(ProductResponseDto p) {
        return p.getOptions().stream().anyMatch(o -> "자유적립식".equals(o.getRsrvTypeNm()));
    }

    private Comparator<ProductResponseDto> rate2Desc() {
        return Comparator.comparingDouble((ProductResponseDto p) ->
                p.getOptions().stream().mapToDouble(ProductOptionDto::getIntrRate2).max().orElse(0.0)).reversed();
    }

    private Comparator<ProductResponseDto> avgIntrRateDesc() {
        return Comparator.comparingDouble((ProductResponseDto p) ->
                p.getOptions().stream().mapToDouble(ProductOptionDto::getIntrRate).average().orElse(0.0)).reversed();
    }

    private boolean hasShortTerm(ProductResponseDto p, int term) {
        return p.getOptions().stream().anyMatch(o -> o.getSaveTrm() <= term);
    }

    private boolean hasLongTerm(ProductResponseDto p, int term) {
        return p.getOptions().stream().anyMatch(o -> o.getSaveTrm() >= term);
    }

    private boolean isHighRate(ProductResponseDto p) {
        return p.getOptions().stream().anyMatch(o -> o.getIntrRate2() >= 2.5);
    }

    private boolean isBigBank(String name) {
        return List.of("국민은행", "신한은행", "하나은행", "우리은행", "농협은행").contains(name);
    }

    private Comparator<ProductResponseDto> rateDesc() {
        return Comparator.comparingDouble((ProductResponseDto p) ->
                p.getOptions().stream().mapToDouble(ProductOptionDto::getIntrRate2).max().orElse(0.0)).reversed();
    }

    private MbtiRecommendResponseDto toDto(ProductResponseDto p, String reason) {
        ProductOptionDto bestOption = p.getOptions().stream()
                .max(Comparator.comparingDouble(ProductOptionDto::getIntrRate2))
                .orElse(null);
        return MbtiRecommendResponseDto.builder()
                .bankName(p.getKorCoNm())
                .productName(p.getFinPrdtNm())
                .intrRate(bestOption != null ? bestOption.getIntrRate() : null)
                .intrRate2(bestOption != null ? bestOption.getIntrRate2() : null)
                .reason(reason)
                .finPrdtCd(p.getFinPrdtCd())
                .build();
    }

    @CacheEvict(value = {"product:detail", "product:list", "product:filter"}, key = "#finPrdtCd", allEntries = false)
    @Override
    public String refreshAndSaveSummary(String finPrdtCd) {
        ProductDetailResponseDto detail = productMapper.getProductDetail(finPrdtCd);
        if (detail == null) throw new IllegalArgumentException("해당 상품이 존재하지 않습니다: " + finPrdtCd);
        List<ProductOptionDto> options = productMapper.getProductOptions(finPrdtCd);
        detail.setOptions(options);
        String material = buildSummarizableText(detail);
        String aiSummary = summarizeService.summarizeTo3Lines(material);
        productMapper.updateProductSummary(finPrdtCd, aiSummary);
        return aiSummary;
    }

    private String buildSummarizableText(ProductDetailResponseDto d) {
        StringBuilder sb = new StringBuilder();
        sb.append("상품명: ").append(d.getProductName()).append("\n");
        sb.append("은행: ").append(d.getBankName()).append("\n");
        if (d.getJoinWay() != null) sb.append("가입방법: ").append(d.getJoinWay()).append("\n");
        if (d.getJoinMember() != null) sb.append("가입대상: ").append(d.getJoinMember()).append("\n");
        if (d.getSpclCnd() != null) sb.append("우대조건: ").append(d.getSpclCnd()).append("\n");
        if (d.getMtrtInt() != null) sb.append("만기후이자: ").append(d.getMtrtInt()).append("\n");
        if (d.getEtcNote() != null) sb.append("비고: ").append(d.getEtcNote()).append("\n");
        if (d.getOptions() != null && !d.getOptions().isEmpty()) {
            double maxRate = d.getOptions().stream()
                    .mapToDouble(o -> o.getIntrRate2() != null ? o.getIntrRate2() : 0.0)
                    .max().orElse(0.0);
            Integer minTrm = d.getOptions().stream()
                    .map(ProductOptionDto::getSaveTrm).filter(java.util.Objects::nonNull)
                    .min(Integer::compareTo).orElse(null);
            Integer maxTrm = d.getOptions().stream()
                    .map(ProductOptionDto::getSaveTrm).filter(java.util.Objects::nonNull)
                    .max(Integer::compareTo).orElse(null);
            sb.append("최대우대금리: ").append(String.format(java.util.Locale.KOREA, "%.2f%%", maxRate)).append("\n");
            if (minTrm != null && maxTrm != null) {
                sb.append("적용기간(개월): ").append(minTrm).append("~").append(maxTrm).append("\n");
            }
            boolean hasFree = d.getOptions().stream().anyMatch(o -> "자유적립식".equals(o.getRsrvTypeNm()));
            if (hasFree) sb.append("자유적립식 가능").append("\n");
        }
        return sb.toString();
    }

    private void tryGenerateSummaryOnce(String finPrdtCd, ProductDetailResponseDto detail) {
        String lockKey = "lock:summary:" + finPrdtCd;
        Boolean ok = srt.opsForValue().setIfAbsent(lockKey, "1", java.time.Duration.ofSeconds(30));
        if (Boolean.TRUE.equals(ok)) {
            new Thread(() -> {
                try {
                    String material = buildSummarizableText(detail);
                    String aiSummary = summarizeService.summarizeTo3Lines(material);
                    productMapper.updateProductSummary(finPrdtCd, aiSummary);
                } catch (Exception ignored) {
                } finally {
                    srt.delete(lockKey);
                }
            }, "ai-summary-" + finPrdtCd).start();
        }
    }
    private void enqueueSummaryIfNeeded(String finPrdtCd, ProductDetailResponseDto d) {
        if (d.getSummary() != null && !d.getSummary().isBlank()) return;
        String lockKey = "lock:ai-summary:" + finPrdtCd;
        if (cacheHelper.tryLock(lockKey, java.time.Duration.ofMinutes(2))) {
            redis.opsForList().leftPush("queue:ai-summary", finPrdtCd);
        }
    }
}
