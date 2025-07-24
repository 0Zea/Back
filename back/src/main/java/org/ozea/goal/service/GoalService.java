package org.ozea.goal.service;

import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.dto.response.GoalDetailResponseDto;
import org.ozea.goal.dto.response.GoalListResponseDto;

import java.util.List;
import java.util.UUID;

public interface GoalService {
    void createGoal(UUID userId, GoalCreateRequestDto request);
    List<GoalListResponseDto> getGoalsByUserId(UUID userId);
    GoalDetailResponseDto getGoalById(UUID goalId);
}
