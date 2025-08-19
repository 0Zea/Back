package org.ozea.goal.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.dto.request.GoalUpdateRequestDto;
import org.ozea.goal.dto.request.LinkAccountRequestDto;
import org.ozea.goal.dto.response.*;
import org.ozea.goal.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/api/goal")
@Tag(name = "Goal")
public class GoalController {
    @Autowired
    private GoalService goalService;

    @GetMapping("/{goalId}/rewarded")
    public ResponseEntity<Boolean> isGoalRewarded(
            @PathVariable UUID goalId,
            @RequestParam("userId") UUID userId
    ) {
        return ResponseEntity.ok(goalService.existsGoalRewardPoint(userId, goalId));
    }
    
    @PostMapping("/{goalId}/claim-reward")
    @Operation(summary = "목표 달성 보상 지급",
            description = "기간 종료 && 목표금액 100% 달성 시 1회 포인트 지급")
    public ResponseEntity<ClaimRewardResponseDto> claimReward(
            @PathVariable UUID goalId,
            @RequestParam("userId") UUID userId
    ) {
        ClaimRewardResponseDto res = goalService.claimGoalReward(userId, goalId);
        return ResponseEntity.ok(res);
    }
    @GetMapping("/past")
    public ResponseEntity<List<PastGoalResponseDto>> getPastGoals(@RequestParam UUID userId) {
        List<PastGoalResponseDto> result = goalService.getPastGoals(userId);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/{goalId}/recommend-products")
    @Operation(summary = "금융상품 추천", description = "목표 ID를 기반으로 금융상품을 추천합니다.")
    public ResponseEntity<List<ProductRecommendResponseDto>> recommendProducts(@PathVariable UUID goalId) {
        List<ProductRecommendResponseDto> recommendations = goalService.recommendProducts(goalId);
        return ResponseEntity.ok(recommendations);
    }
    @GetMapping("/accounts/{userId}")
    @Operation(summary = "사용자 계좌 목록 조회", description = "사용자의 보유 계좌를 조회합니다.")
    public ResponseEntity<?> getAccountsByUser(@PathVariable UUID userId) {
        List<LinkedAccountDto> accounts = goalService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }
    @DeleteMapping("/unlink/{accountId}")
    @Operation(summary = "계좌 연동 해제", description = "해당 계좌를 목표에서 연동 해제합니다.")
    public ResponseEntity<?> unlinkAccount(@PathVariable int accountId) {
        goalService.unlinkAccount(accountId);
        return ResponseEntity.ok("계좌 연동이 해제되었습니다.");
    }
    @PostMapping("/{goalId}/link-account")
    @Operation(summary = "목표 계좌 연동", description = "사용자의 목표에 계좌를 연동합니다.")
    public ResponseEntity<Void> linkAccountToGoal(
            @PathVariable UUID goalId,
            @RequestBody LinkAccountRequestDto requestDto
    ) {
        goalService.linkAccountToGoal(goalId, requestDto.getAccountId());
        return ResponseEntity.ok().build();
    }
    @PostMapping
    @Operation(summary = "목표 생성", description = "사용자의 목표를 생성합니다.")
    public ResponseEntity<?> createGoal(@RequestBody GoalCreateRequestDto request,
                                        @RequestParam UUID userId) {
        try {
            UUID goalId = UUID.randomUUID();
            goalService.createGoal(userId, request, goalId);
            return ResponseEntity.ok(Map.of(
                    "message", "목표가 성공적으로 등록되었습니다.",
                    "goal_id", goalId.toString()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "목표 생성 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    @GetMapping
    @Operation(summary = "목표 전체 조회", description = "사용자의 목표 목록을 조회합니다.")
    public ResponseEntity<?> getAllGoals(@RequestParam UUID userId) {
        List<GoalListResponseDto> goals = goalService.getGoalsByUserId(userId);
        return ResponseEntity.ok(goals);
    }
    @GetMapping("/{goalId}")
    @Operation(summary = "목표 상세 조회", description = "사용자 목표 상세 정보를 조회합니다.")
    public ResponseEntity<?> getGoalDetail(@PathVariable UUID goalId) {
        GoalDetailResponseDto goal = goalService.getGoalById(goalId);
        return ResponseEntity.ok(goal);
    }
    @DeleteMapping("/{goalId}")
    @Operation(summary = "목표 삭제", description = "goalId를 기반으로 목표를 삭제합니다.")
    public ResponseEntity<?> deleteGoal(@PathVariable UUID goalId) {
        goalService.deleteGoal(goalId);
        return ResponseEntity.ok(Map.of("message", "목표가 삭제되었습니다."));
    }
    @PutMapping("/{goalId}")
    @Operation(summary = "목표 수정", description = "goalId에 해당하는 목표를 수정합니다.")
    public ResponseEntity<?> updateGoal(@PathVariable UUID goalId,
                                        @RequestBody GoalUpdateRequestDto dto) {
        goalService.updateGoal(goalId , dto);
        return ResponseEntity.ok(Map.of("message", "목표가 수정되었습니다."));
    }
    @GetMapping("/recommend-next")
    @Operation(summary = "다음 목표 추천", description = "이전 목표를 기반으로 추천 목표 정보를 제공합니다.")
    public ResponseEntity<RecommendNextGoalDto> recommendNextGoal(@RequestParam UUID userId) {
        RecommendNextGoalDto dto = goalService.recommendNextGoal(userId);
        return ResponseEntity.ok(dto);
    }
}
