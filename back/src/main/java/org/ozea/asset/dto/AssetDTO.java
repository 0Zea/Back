package org.ozea.asset.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ozea.asset.domain.AssetVO;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDTO {

    private String name;              // 사용자 이름
    private BigInteger totalAssets;         // 총자산
    private BigInteger monthlyNetIncome;    // 월 순수익
    private Double averageGoalRate;   // 목표 평균 달성률

    // DTO -> VO
    public AssetVO toVO() {
        return AssetVO.builder()
                .name(name)
                .totalAssets(totalAssets)
                .monthlyNetIncome(monthlyNetIncome)
                .averageGoalRate(averageGoalRate)
                .build();
    }

    // VO -> DTO
    public static AssetDTO of(AssetVO assetVO) {
        return AssetDTO.builder()
                .name(assetVO.getName())
                .totalAssets(assetVO.getTotalAssets())
                .monthlyNetIncome(assetVO.getMonthlyNetIncome())
                .averageGoalRate(assetVO.getAverageGoalRate())
                .build();
    }
}
