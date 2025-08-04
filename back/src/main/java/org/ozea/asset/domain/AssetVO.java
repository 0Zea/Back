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

    private String name;
    private BigInteger totalAssets;
    private BigInteger monthlyNetIncome;
    private Double averageGoalRate;
}
