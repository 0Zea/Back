package org.ozea.goal.mapper;

import org.apache.ibatis.annotations.Param;
import org.ozea.goal.domain.Goal;
import org.ozea.goal.dto.request.GoalUpdateRequestDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GoalMapper {
    List<Goal> findAllByUserId(UUID userId);
    Goal findByGoalId(UUID goalId);
    int deleteByGoalIdAndUserId(@Param("goalId") UUID goalId, @Param("userId") UUID userId);
    int updateGoalByIdAndUserId(@Param("goalId") UUID goalId,
                                @Param("userId") UUID userId,
                                @Param("dto") GoalUpdateRequestDto dto);
    void insertGoal(Goal goal);
    long sumTargetAmountOverlappingGoals(@Param("userId") UUID userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);


} 