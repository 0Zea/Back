package org.ozea.goal.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.ozea.goal.domain.Goal;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@ApiModel(description = "목표 생성 요청 DTO") // ← 클래스 설명
public class GoalCreateRequestDto {

    @ApiModelProperty(value = "목표 이름", example = "내 집 마련")
    private String goal_name;

    @ApiModelProperty(value = "목표 금액", example = "50000000")
    private Long target_amount;

    @ApiModelProperty(value = "저축한 금액", example = "0")
    private Long save_amount = 0L; // 프론트에서 안 넣으면 자동 0

    @ApiModelProperty(value = "시작일", example = "2025-07-24")
    private LocalDate start_date;

    @ApiModelProperty(value = "종료일", example = "2026-07-24")
    private LocalDate end_date;

    @ApiModelProperty(value = "입금 날짜", example = "15") // 매달 15일
    private int deposit_date;

    public Goal toEntity(UUID userId) {
        return Goal.builder()
                .goalId(UUID.randomUUID())
                .userId(userId)
                .goalName(goal_name)
                .targetAmount(target_amount)
                .saveAmount(save_amount)
                .startDate(start_date)
                .endDate(end_date)
                .depositDate(deposit_date)
                .status("진행")
                .build();
    }
}
