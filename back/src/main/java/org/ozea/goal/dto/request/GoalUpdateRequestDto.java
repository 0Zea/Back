package org.ozea.goal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Schema(name = "GoalUpdateRequestDto", description = "목표 수정 요청 DTO")
public class GoalUpdateRequestDto {

    @Schema(description = "목표 이름", example = "유럽 여행 자금")
    private String goal_name;

    @Schema(description = "목표 금액", example = "4000000")
    private Long target_amount;

    @Schema(description = "시작일자", example = "2025-08-01")
    private LocalDate start_date;

    @Schema(description = "종료일자", example = "2026-08-01")
    private LocalDate end_date;

    @Schema(description = "입금 날짜(매월)", example = "15")
    private int deposit_date;
}