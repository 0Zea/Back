package org.ozea.goal.service;

import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.dto.request.GoalUpdateRequestDto;
import org.ozea.goal.dto.response.GoalDetailResponseDto;
import org.ozea.goal.dto.response.GoalListResponseDto;
import org.ozea.goal.dto.response.LinkedAccountDto;
import org.ozea.goal.dto.response.ProductRecommendResponseDto;

import java.util.List;
import java.util.UUID;

public interface GoalService {
    void createGoal(UUID userId, GoalCreateRequestDto request);
    List<GoalListResponseDto> getGoalsByUserId(UUID userId);
    GoalDetailResponseDto getGoalById(UUID goalId);
    List<LinkedAccountDto> getAccountsByUserId(UUID userId);
    void deleteGoal(UUID goalId);
    void updateGoal(UUID goalId, GoalUpdateRequestDto dto);
    void linkAccountToGoal(UUID goalId, int accountId);
    void unlinkAccount(int accountId);
    List<ProductRecommendResponseDto> recommendProducts(UUID goalId);
}
