package org.ozea.goal.mapper;

import org.apache.ibatis.annotations.Param;
import org.ozea.goal.domain.Goal;
import org.ozea.goal.dto.request.GoalUpdateRequestDto;
import org.ozea.goal.dto.response.GoalDetailResponseDto;
import org.ozea.goal.dto.response.LinkedAccountDto;
import org.ozea.goal.dto.response.ProductRecommendResponseDto;

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
    int isAccountAlreadyLinked(int accountId);

    void linkAccountToGoal(@Param("goalId") UUID goalId, @Param("accountId") int accountId);
    List<LinkedAccountDto> findLinkedAccountsByGoalId(@Param("goalId") UUID goalId);
    void unlinkAccount(int accountId);
    List<LinkedAccountDto> findAccountsByUserId(UUID userId);
    void unlinkAllAccountsFromGoal(UUID goalId);
    List<ProductRecommendResponseDto> findProductsWithOptions();

} 