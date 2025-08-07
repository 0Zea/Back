package org.ozea.goal.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.ozea.goal.domain.Goal;
import java.time.LocalDate;
import java.util.List;
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
    private List<LinkedAccountDto> linked_accounts;
    public static GoalDetailResponseDto from(Goal goal, List<LinkedAccountDto> accounts) {
        return GoalDetailResponseDto.builder()
                .goal_id(goal.getGoalId())
                .goal_name(goal.getGoalName())
                .target_amount(goal.getTargetAmount())
                .current_amount(goal.getSaveAmount())
                .start_date(goal.getStartDate())
                .end_date(goal.getEndDate())
                .deposit_date(goal.getDepositDate())
                .linked_accounts(accounts)
                .build();
    }
}
