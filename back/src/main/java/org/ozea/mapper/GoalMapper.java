package org.ozea.mapper;

import org.ozea.goal.domain.Goal;

import java.util.List;
import java.util.UUID;

public interface GoalMapper {
    void insertGoal(Goal goal);
    List<Goal> findAllByUserId(UUID userId);
}
