package org.ozea.asset.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetVO {

    private String name;             // USER.name
    private BigInteger totalAssets;        // 총자산 (계산된 값)
    private BigInteger monthlyNetIncome;   // 월 순수익 (salary - pay_amount)
    private Double averageGoalRate;  // 목표 평균 달성률
}
