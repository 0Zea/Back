package org.ozea.goal.service;

import org.ozea.user.domain.User;
import org.ozea.goal.domain.Goal;
import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.dto.request.GoalUpdateRequestDto;
import org.ozea.goal.dto.response.GoalDetailResponseDto;
import org.ozea.goal.dto.response.GoalListResponseDto;
import org.ozea.goal.mapper.GoalMapper;
import org.ozea.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    public void createGoal(UUID userId, GoalCreateRequestDto requestDto) {
        // 1. 목표 기간 계산
        LocalDate startDate = requestDto.getStart_date();
        LocalDate endDate = requestDto.getEnd_date();
        long totalMonths = ChronoUnit.MONTHS.between(startDate, endDate) + 1;

        // 2. 사용자 정보 조회
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자가 존재하지 않습니다.");
        }

        // 3. 월 순이익 계산
        long monthlyIncome = user.getSalary() - user.getPayAmount();
        long totalExpectedIncome = monthlyIncome * totalMonths;

        // 4. 기존 목표 금액 합산
        long existingTargetAmount = goalMapper.sumTargetAmountOverlappingGoals(userId, startDate, endDate);

        // 5. 새 목표 금액
        long newTargetAmount = requestDto.getTarget_amount();

        // 6. 목표 가능 여부 판단
        if (existingTargetAmount + newTargetAmount > totalExpectedIncome) {
            throw new IllegalArgumentException("해당 기간 순이익을 초과하여 목표를 생성할 수 없습니다.");
        }

        // 7. insert
        goalMapper.insertGoal(requestDto.toEntity(userId));
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
        return GoalDetailResponseDto.from(goal);
    }

    @Override
    public void deleteGoal(UUID goalId, UUID userId) {
        int deleted = goalMapper.deleteByGoalIdAndUserId(goalId, userId);
        if (deleted == 0) {
            throw new IllegalArgumentException("해당 목표가 존재하지 않거나 권한이 없습니다.");
        }
    }

    @Override
    public void updateGoal(UUID goalId, UUID userId, GoalUpdateRequestDto dto) {
        int updated = goalMapper.updateGoalByIdAndUserId(goalId, userId, dto);
        if (updated == 0) {
            throw new IllegalArgumentException("해당 목표가 존재하지 않거나 권한이 없습니다.");
        }
    }




}
