package org.ozea.goal.domain;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {
    private UUID goalId;
    private UUID userId;
    private String goalName;
    private Long targetAmount;
    private Long saveAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private int depositDate;
}
