package org.ozea.goal.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PastGoalResponseDto {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long targetAmount;
    private boolean success; // true: 성공, false: 실패
}
