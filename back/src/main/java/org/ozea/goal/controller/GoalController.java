package org.ozea.goal.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.dto.request.GoalUpdateRequestDto;
import org.ozea.goal.dto.request.LinkAccountRequestDto;
import org.ozea.goal.dto.response.GoalDetailResponseDto;
import org.ozea.goal.dto.response.GoalListResponseDto;
import org.ozea.goal.dto.response.LinkedAccountDto;
import org.ozea.goal.dto.response.ProductRecommendResponseDto;
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

    @GetMapping("/{goalId}/recommend-products")
    @ApiOperation(value = "금융상품 추천", notes = "목표 ID를 기반으로 금융상품을 추천합니다.")
    public ResponseEntity<List<ProductRecommendResponseDto>> recommendProducts(@PathVariable UUID goalId) {
        List<ProductRecommendResponseDto> recommendations = goalService.recommendProducts(goalId);
        return ResponseEntity.ok(recommendations);
    }


    @GetMapping("/accounts/{userId}")
    @ApiOperation(value = "사용자 계좌 목록 조회", notes = "사용자의 보유 계좌를 조회합니다.")
    public ResponseEntity<?> getAccountsByUser(@PathVariable UUID userId) {
        List<LinkedAccountDto> accounts = goalService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @DeleteMapping("/unlink/{accountId}")
    @ApiOperation(value = "계좌 연동 해제", notes = "해당 계좌를 목표에서 연동 해제합니다.")
    public ResponseEntity<?> unlinkAccount(@PathVariable int accountId) {
        goalService.unlinkAccount(accountId);
        return ResponseEntity.ok("계좌 연동이 해제되었습니다.");
    }

    @PostMapping("/{goalId}/link-account")
    @ApiOperation(value = "목표 계좌 연동", notes = "사용자의 목표에 계좌를 연동합니다.")
    public ResponseEntity<Void> linkAccountToGoal(
            @PathVariable UUID goalId,
            @RequestBody LinkAccountRequestDto requestDto
    ) {
        goalService.linkAccountToGoal(goalId, requestDto.getAccountId());
        return ResponseEntity.ok().build();
    }

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

    @DeleteMapping("/{goalId}")
    @ApiOperation(value = "목표 삭제", notes = "goalId와 userId를 기반으로 목표를 삭제합니다.")
    public ResponseEntity<?> deleteGoal(@PathVariable UUID goalId,
                                        @RequestParam UUID userId) {
        goalService.deleteGoal(goalId, userId);
        return ResponseEntity.ok(Map.of("message", "목표가 삭제되었습니다."));
    }

    @PutMapping("/{goalId}")
    @ApiOperation(value = "목표 수정", notes = "goalId에 해당하는 목표를 수정합니다.")
    public ResponseEntity<?> updateGoal(@PathVariable UUID goalId,
                                        @RequestParam UUID userId,
                                        @RequestBody GoalUpdateRequestDto dto) {
        goalService.updateGoal(goalId, userId, dto);
        return ResponseEntity.ok(Map.of("message", "목표가 수정되었습니다."));
    }

}
