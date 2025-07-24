package org.ozea.goal.service;

import org.ozea.goal.domain.Goal;
import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.dto.response.GoalResponseDto;
import org.ozea.mapper.GoalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public List<GoalResponseDto> getGoalsByUserId(UUID userId) {
        List<Goal> goals = goalMapper.findAllByUserId(userId);
        return goals.stream()
                .map(GoalResponseDto::from)
                .collect(Collectors.toList());
    }

}
