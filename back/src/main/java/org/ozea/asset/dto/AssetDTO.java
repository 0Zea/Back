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
    private String name;
    private BigInteger totalAssets;
    private BigInteger monthlyNetIncome;
    private Double averageGoalRate;   // 목표 평균 달성률
    public AssetVO toVO() {
        return AssetVO.builder()
                .name(name)
                .totalAssets(totalAssets)
                .monthlyNetIncome(monthlyNetIncome)
                .averageGoalRate(averageGoalRate)
                .build();
    }
    public static AssetDTO of(AssetVO assetVO) {
        return AssetDTO.builder()
                .name(assetVO.getName())
                .totalAssets(assetVO.getTotalAssets())
                .monthlyNetIncome(assetVO.getMonthlyNetIncome())
                .averageGoalRate(assetVO.getAverageGoalRate())
                .build();
    }
}
