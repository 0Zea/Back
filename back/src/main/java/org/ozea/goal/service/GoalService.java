package org.ozea.goal.service;
import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.dto.request.GoalUpdateRequestDto;
import org.ozea.goal.dto.response.*;
import java.util.List;
import java.util.UUID;
public interface GoalService {
    void createGoal(UUID userId, GoalCreateRequestDto request, UUID goalId);
    List<GoalListResponseDto> getGoalsByUserId(UUID userId);
    GoalDetailResponseDto getGoalById(UUID goalId);
    List<LinkedAccountDto> getAccountsByUserId(UUID userId);
    void deleteGoal(UUID goalId);
    void updateGoal(UUID goalId, GoalUpdateRequestDto dto);
    void linkAccountToGoal(UUID goalId, int accountId);
    void unlinkAccount(int accountId);
    List<ProductRecommendResponseDto> recommendProducts(UUID goalId);
    List<PastGoalResponseDto> getPastGoals(UUID userId);
    RecommendNextGoalDto recommendNextGoal(UUID userId);
    void updateGoalStatuses(); // 목표 상태 자동 업데이트
}
