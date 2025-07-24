package org.ozea.goal.dto.response;

import lombok.*;
import org.ozea.goal.domain.Goal;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GoalResponseDto {
    private UUID goalId;
    private String goalName;
    private Long targetAmount;
    private Long saveAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    public static GoalResponseDto from(Goal goal) {
        return GoalResponseDto.builder()
                .goalId(goal.getGoalId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .saveAmount(goal.getSaveAmount())
                .startDate(goal.getStartDate())
                .endDate(goal.getEndDate())
                .status(goal.getStatus())
                .build();
    }
}
