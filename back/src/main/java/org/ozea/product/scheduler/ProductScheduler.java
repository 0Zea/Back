package org.ozea.product.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ozea.product.dto.response.ProductDetailResponseDto;
import org.ozea.product.dto.response.ProductOptionDto;
import org.ozea.product.mapper.ProductMapper;
import org.ozea.ai.service.OpenAISummarizeServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductScheduler {
    private final RedisTemplate<String,Object> redis;
    private final ProductMapper productMapper;
    private final OpenAISummarizeServiceImpl summarizeService;

    @Scheduled(fixedDelay = 1000)
    public void consumeSummaryJobs() {
        String finPrdtCd = (String) redis.opsForList().rightPop("queue:ai-summary");
        if (finPrdtCd == null) return;

        try {
            ProductDetailResponseDto d = productMapper.getProductDetail(finPrdtCd);
            if (d == null) return;
            List<ProductOptionDto> opts = productMapper.getProductOptions(finPrdtCd);
            d.setOptions(opts);

            String material = buildMaterial(d);
            String summary  = summarizeService.summarizeTo3Lines(material);
            productMapper.updateProductSummary(finPrdtCd, summary);

            redis.delete("product:detail::" + finPrdtCd);
            log.info("AI summary saved & cache evicted for {}", finPrdtCd);
        } catch (Exception e) {
            log.warn("AI summary job failed for {}: {}", finPrdtCd, e.getMessage());
        }
    }

    private String buildMaterial(ProductDetailResponseDto d) {
        StringBuilder sb = new StringBuilder();
        sb.append("상품명: ").append(d.getProductName()).append("\n");
        sb.append("은행: ").append(d.getBankName()).append("\n");
        if (d.getJoinWay()!=null) sb.append("가입방법: ").append(d.getJoinWay()).append("\n");
        if (d.getJoinMember()!=null) sb.append("가입대상: ").append(d.getJoinMember()).append("\n");
        if (d.getSpclCnd()!=null) sb.append("우대조건: ").append(d.getSpclCnd()).append("\n");
        if (d.getMtrtInt()!=null) sb.append("만기후이자: ").append(d.getMtrtInt()).append("\n");
        if (d.getEtcNote()!=null) sb.append("비고: ").append(d.getEtcNote()).append("\n");
        if (d.getOptions()!=null && !d.getOptions().isEmpty()) {
            double maxRate = d.getOptions().stream().mapToDouble(o -> o.getIntrRate2()!=null?o.getIntrRate2():0.0).max().orElse(0);
            Integer minTrm = d.getOptions().stream().map(ProductOptionDto::getSaveTrm).filter(java.util.Objects::nonNull).min(Integer::compareTo).orElse(null);
            Integer maxTrm = d.getOptions().stream().map(ProductOptionDto::getSaveTrm).filter(java.util.Objects::nonNull).max(Integer::compareTo).orElse(null);
            sb.append("최대우대금리: ").append(String.format(java.util.Locale.KOREA,"%.2f%%",maxRate)).append("\n");
            if (minTrm!=null && maxTrm!=null) sb.append("적용기간(개월): ").append(minTrm).append("~").append(maxTrm).append("\n");
            boolean hasFree = d.getOptions().stream().anyMatch(o -> "자유적립식".equals(o.getRsrvTypeNm()));
            if (hasFree) sb.append("자유적립식 가능\n");
        }
        return sb.toString();
    }
}