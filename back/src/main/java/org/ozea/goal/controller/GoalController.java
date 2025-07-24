package org.ozea.goal.controller;

import io.swagger.annotations.Api;
import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/goal")
@Api (tags = "Goal")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @PostMapping
    public ResponseEntity<?> createGoal(@RequestBody GoalCreateRequestDto request,
                                        @RequestParam UUID userId) {
        goalService.createGoal(userId, request);
        return ResponseEntity.ok(Map.of("message", "목표가 성공적으로 등록되었습니다."));
    }
}
