package org.ozea.goal.service;

import org.ozea.goal.dto.request.GoalCreateRequestDto;

import java.util.UUID;

public interface GoalService {
    void createGoal(UUID userId, GoalCreateRequestDto request);
}
