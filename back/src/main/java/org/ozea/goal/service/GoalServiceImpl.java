package org.ozea.goal.service;

import org.ozea.goal.domain.Goal;
import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.mapper.GoalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GoalServiceImpl implements GoalService {

    @Autowired
    private GoalMapper goalMapper;

    @Override
    public void createGoal(UUID userId, GoalCreateRequestDto request) {
        Goal goal = request.toEntity(userId);
        goalMapper.insertGoal(goal);
    }
}
