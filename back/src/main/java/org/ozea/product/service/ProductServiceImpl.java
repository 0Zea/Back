package org.ozea.product.service;

import lombok.RequiredArgsConstructor;
import org.ozea.product.dto.response.MbtiRecommendResponseDto;
import org.ozea.product.dto.response.ProductOptionDto;
import org.ozea.product.dto.response.ProductResponseDto;
import org.ozea.product.mapper.ProductMapper;
import org.ozea.user.domain.User;
import org.ozea.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;


    @Override
    public List<ProductResponseDto> getAllProductsWithOptions() {
        return productMapper.findAllProductsWithOptions();
    }

    @Override
    public List<MbtiRecommendResponseDto> getRecommendedProductsByMbti(UUID userId) {
        User user = userMapper.findById(userId);
        if (user == null || user.getMbti() == null) {
            throw new IllegalArgumentException("사용자의 MBTI 정보가 없습니다.");
        }

        String mbti = user.getMbti();
        List<ProductResponseDto> products = productMapper.findAllProductsWithOptions();

        return switch (mbti) {
            case "신속한 승부사" -> products.stream()
                    .filter(p -> hasShortTerm(p, 6) && isFreeSaving(p) && isHighRate(p))
                    .sorted(rate2Desc())
                    .limit(5)
                    .map(p -> toDto(p, "단기 + 자유 적립 고금리 상품"))
                    .toList();

            case "신중한 승부사" -> products.stream()
                    .filter(p -> hasLongTerm(p, 12) && isBigBank(p.getKorCoNm()))
                    .sorted(avgIntrRateDesc())
                    .limit(5)
                    .map(p -> toDto(p, "안정적이고 장기적인 상품"))
                    .toList();

            case "신속한 분석가" -> products.stream()
                    .filter(p -> hasShortTerm(p, 6))
                    .sorted(rateDesc())
                    .limit(5)
                    .map(p -> toDto(p, "짧고 빠르게 회전 가능한 상품"))
                    .toList();

            case "신중한 분석가" -> products.stream()
                    .filter(p -> hasLongTerm(p, 12))
                    .sorted(rate2Desc())
                    .limit(5)
                    .map(p -> toDto(p, "장기 분석 기반 추천"))
                    .toList();

            default -> throw new IllegalArgumentException("알 수 없는 MBTI: " + mbti);
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
                .build();
    }
}


