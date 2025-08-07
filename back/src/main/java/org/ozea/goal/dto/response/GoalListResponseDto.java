package org.ozea.goal.dto.response;
import lombok.*;
import org.ozea.goal.domain.Goal;
import java.time.LocalDate;
import java.util.UUID;
@Getter
@Builder
@AllArgsConstructor
public class GoalListResponseDto {
    private UUID goal_id;
    private String goal_name;
    private Long target_amount;
    private Long save_amount;
    private LocalDate start_date;
    private LocalDate end_date;
    public static GoalListResponseDto from(Goal goal) {
        return GoalListResponseDto.builder()
                .goal_id(goal.getGoalId())
                .goal_name(goal.getGoalName())
                .target_amount(goal.getTargetAmount())
                .save_amount(goal.getSaveAmount())
                .start_date(goal.getStartDate())
                .end_date(goal.getEndDate())
                .build();
    }
}
