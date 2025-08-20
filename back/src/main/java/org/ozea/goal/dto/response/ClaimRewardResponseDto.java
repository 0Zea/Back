package org.ozea.goal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClaimRewardResponseDto {
    public enum Status { REWARDED, ALREADY_REWARDED, NOT_ELIGIBLE }
    private Status status;
    private Integer points;
}
