package org.ozea.goal.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@ApiModel(description = "목표 수정 요청 DTO")
public class GoalUpdateRequestDto {

    @ApiModelProperty(value = "목표 이름", example = "유럽 여행 자금")
    private String goal_name;

    @ApiModelProperty(value = "목표 금액", example = "4000000")
    private Long target_amount;

    @ApiModelProperty(value = "시작일", example = "2025-08-01")
    private LocalDate start_date;

    @ApiModelProperty(value = "종료일", example = "2026-08-01")
    private LocalDate end_date;

    @ApiModelProperty(value = "입금 날짜", example = "15")
    private int deposit_date;
}
