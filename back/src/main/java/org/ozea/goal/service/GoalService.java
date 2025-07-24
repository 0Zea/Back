package org.ozea.goal.service;

import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.dto.response.GoalResponseDto;

import java.util.List;
import java.util.UUID;

public interface GoalService {
    void createGoal(UUID userId, GoalCreateRequestDto request);
    List<GoalResponseDto> getGoalsByUserId(UUID userId);

}
