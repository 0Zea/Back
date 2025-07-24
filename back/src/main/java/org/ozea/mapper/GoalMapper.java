package org.ozea.mapper;

import org.apache.ibatis.annotations.Param;
import org.ozea.goal.domain.Goal;
import org.ozea.goal.dto.request.GoalUpdateRequestDto;

import java.util.List;
import java.util.UUID;

public interface GoalMapper {
    void insertGoal(Goal goal);
    List<Goal> findAllByUserId(UUID userId);
    Goal findByGoalId(UUID goalId);
    int deleteByGoalIdAndUserId(@Param("goalId") UUID goalId, @Param("userId") UUID userId);
    int updateGoalByIdAndUserId(@Param("goalId") UUID goalId,
                                @Param("userId") UUID userId,
                                @Param("dto") GoalUpdateRequestDto dto);

}
