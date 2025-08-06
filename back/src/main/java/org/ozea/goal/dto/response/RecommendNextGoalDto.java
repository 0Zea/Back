package org.ozea.goal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RecommendNextGoalDto {
    private Long recommendedAmount;
    private LocalDate recommendedStartDate;
    private LocalDate recommendedEndDate;
    private String reason;
}
