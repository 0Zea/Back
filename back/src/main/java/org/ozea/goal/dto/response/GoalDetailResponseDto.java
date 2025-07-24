package org.ozea.goal.dto.response;

import lombok.*;
import org.ozea.goal.domain.Goal;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GoalDetailResponseDto {

    private UUID goal_id;
    private String goal_name;
    private Long target_amount;
    private Long current_amount;
    private LocalDate start_date;
    private LocalDate end_date;
    private int deposit_date;

    public static GoalDetailResponseDto from(Goal goal) {
        return GoalDetailResponseDto.builder()
                .goal_id(goal.getGoalId())
                .goal_name(goal.getGoalName())
                .target_amount(goal.getTargetAmount())
                .current_amount(goal.getSaveAmount())
                .start_date(goal.getStartDate())
                .end_date(goal.getEndDate())
                .deposit_date(goal.getDepositDate())
                .build();
    }
}
