package org.ozea.goal.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.dto.response.GoalDetailResponseDto;
import org.ozea.goal.dto.response.GoalListResponseDto;
import org.ozea.goal.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/goal")
@Api (tags = "Goal")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @PostMapping
    @ApiOperation(value = "목표 생성", notes = "사용자의 목표를 생성합니다.")
    public ResponseEntity<?> createGoal(@RequestBody GoalCreateRequestDto request,
                                        @RequestParam UUID userId) {
        goalService.createGoal(userId, request);
        return ResponseEntity.ok(Map.of("message", "목표가 성공적으로 등록되었습니다."));
    }

    @GetMapping
    @ApiOperation(value = "목표 전체 조회", notes = "사용자의 목표 목록을 조회합니다.")
    public ResponseEntity<?> getAllGoals(@RequestParam UUID userId) {
        List<GoalListResponseDto> goals = goalService.getGoalsByUserId(userId);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{goalId}")
    @ApiOperation(value = "목표 상세 조회", notes = "사용자 목표 상세 정보를 조회합니다.")
    public ResponseEntity<?> getGoalDetail(@PathVariable UUID goalId) {
        GoalDetailResponseDto goal = goalService.getGoalById(goalId);
        return ResponseEntity.ok(goal);
    }



}
