package org.ozea.goal.service;

import org.ozea.goal.dto.response.*;
import org.ozea.user.domain.User;
import org.ozea.goal.domain.Goal;
import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.dto.request.GoalUpdateRequestDto;
import org.ozea.goal.mapper.GoalMapper;
import org.ozea.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GoalServiceImpl implements GoalService {

    @Autowired
    private GoalMapper goalMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<PastGoalResponseDto> getPastGoals(UUID userId) {
        return goalMapper.findPastGoalsByUserId(userId);
    }

    @Override
    public void createGoal(UUID userId, GoalCreateRequestDto requestDto, UUID goalId) {
        LocalDate startDate = requestDto.getStart_date();
        LocalDate endDate = requestDto.getEnd_date();
        long totalMonths = ChronoUnit.MONTHS.between(startDate, endDate) + 1;

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자가 존재하지 않습니다.");
        }

        long monthlyIncome = user.getSalary() - user.getPayAmount();
        long totalExpectedIncome = monthlyIncome * totalMonths;

        long existingTargetAmount = goalMapper.sumTargetAmountOverlappingGoals(userId, startDate, endDate);

        long newTargetAmount = requestDto.getTarget_amount();

        if (existingTargetAmount + newTargetAmount > totalExpectedIncome) {
            throw new IllegalArgumentException("해당 기간 순이익을 초과하여 목표를 생성할 수 없습니다.");
        }
        goalMapper.insertGoal(requestDto.toEntity(userId, goalId));
    }

    @Override
    public List<ProductRecommendResponseDto> recommendProducts(UUID goalId) {
        Goal goal = goalMapper.findByGoalId(goalId);
        if (goal == null) {
            throw new IllegalArgumentException("해당 목표가 존재하지 않습니다.");
        }

        int goalDuration = Period.between(goal.getStartDate(), goal.getEndDate()).getYears();
        long goalAmount = goal.getTargetAmount();
        long savePerMonth = goalAmount / (goalDuration * 12);

        List<ProductRecommendResponseDto> candidates = goalMapper.findProductsWithOptions();

        return candidates.stream()
                .sorted(Comparator.comparing(ProductRecommendResponseDto::getIntrRate2).reversed())
                .limit(5)
                .peek(dto -> {
                    StringBuilder reason = new StringBuilder();
                    reason.append("✨ 목표 기간과 비슷한 ").append(dto.getSaveTrm()).append("개월 상품입니다.");
                    if (dto.getIntrRate2().compareTo(new BigDecimal("3.0")) > 0) {
                        reason.append(" 우대금리가 높습니다.");
                    }
                    if (dto.getRsrvTypeNm().contains("자유")) reason.append(" 자유적립식입니다.");
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<LinkedAccountDto> getAccountsByUserId(UUID userId) {
        return goalMapper.findAccountsByUserId(userId);
    }

    @Override
    public void linkAccountToGoal(UUID goalId, int accountId) {
        Integer count = goalMapper.isAccountAlreadyLinked(accountId);
        if (count != null && count > 0) {
            throw new IllegalStateException("이미 다른 목표에 연동된 계좌입니다.");
        }
        goalMapper.linkAccountToGoal(goalId, accountId);
    }

    @Override
    public List<GoalListResponseDto> getGoalsByUserId(UUID userId) {
        List<Goal> goals = goalMapper.findAllByUserId(userId);
        return goals.stream()
                .map(GoalListResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public GoalDetailResponseDto getGoalById(UUID goalId) {
        Goal goal = goalMapper.findByGoalId(goalId);
        if (goal == null) {
            throw new NoSuchElementException("해당 목표가 존재하지 않습니다.");
        }

        List<LinkedAccountDto> linkedAccounts = goalMapper.findLinkedAccountsByGoalId(goalId);

        return GoalDetailResponseDto.from(goal, linkedAccounts);
    }

    @Override
    public void unlinkAccount(int accountId) {
        goalMapper.unlinkAccount(accountId);
    }

    @Override
    public void deleteGoal(UUID goalId) {
        goalMapper.unlinkAllAccountsFromGoal(goalId);

        int deleted = goalMapper.deleteByGoalIdAndUserId(goalId);
        if (deleted == 0) {
            throw new IllegalArgumentException("해당 목표가 존재하지 않거나 권한이 없습니다.");
        }
    }

    @Override
    public void updateGoal(UUID goalId, GoalUpdateRequestDto dto) {
        int updated = goalMapper.updateGoalByIdAndUserId(goalId, dto);
        if (updated == 0) {
            throw new IllegalArgumentException("해당 목표가 존재하지 않거나 권한이 없습니다.");
        }
    }
}
