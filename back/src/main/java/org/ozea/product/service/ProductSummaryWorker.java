package org.ozea.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ozea.ai.service.OpenAISummarizeServiceImpl;
import org.ozea.common.cache.CacheHelper;
import org.ozea.product.dto.response.ProductDetailResponseDto;
import org.ozea.product.mapper.ProductMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSummaryWorker {

    private final OpenAISummarizeServiceImpl summarizeService;
    private final ProductMapper productMapper;
    private final StringRedisTemplate srt;
    private final CacheHelper cacheHelper;

    @Async("taskExecutor")
    public void generateSummaryOnce(String finPrdtCd, ProductDetailResponseDto detail) {
        final String lockKey = "lock:ai-summary:" + finPrdtCd;
        try {
            String material = buildSummarizableText(detail);
            String aiSummary = summarizeService.summarizeTo3Lines(material);
            productMapper.updateProductSummary(finPrdtCd, aiSummary);
            srt.opsForValue().set("done:ai-summary:" + finPrdtCd, "1");
        } catch (Exception e) {
            log.warn("AI Summary failed for {}", finPrdtCd, e);
        } finally {
            cacheHelper.unlock(lockKey);
        }
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
            Integer minTrm = d.getOptions().stream().map(org.ozea.product.dto.response.ProductOptionDto::getSaveTrm)
                    .filter(java.util.Objects::nonNull).min(Integer::compareTo).orElse(null);
            Integer maxTrm = d.getOptions().stream().map(org.ozea.product.dto.response.ProductOptionDto::getSaveTrm)
                    .filter(java.util.Objects::nonNull).max(Integer::compareTo).orElse(null);
            sb.append("옵션요약: 최고금리 ").append(maxRate).append("%, 기간 ")
                    .append(minTrm).append("~").append(maxTrm).append("개월\n");
        }
        return sb.toString();
    }
}