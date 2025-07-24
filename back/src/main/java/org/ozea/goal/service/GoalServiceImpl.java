package org.ozea.goal.service;

import org.ozea.goal.domain.Goal;
import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.dto.response.GoalDetailResponseDto;
import org.ozea.goal.dto.response.GoalListResponseDto;
import org.ozea.mapper.GoalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GoalServiceImpl implements GoalService {

    @Autowired
    private GoalMapper goalMapper;

    @Override
    public void createGoal(UUID userId, GoalCreateRequestDto request) {
        Goal goal = request.toEntity(userId);
        goalMapper.insertGoal(goal);
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


}
