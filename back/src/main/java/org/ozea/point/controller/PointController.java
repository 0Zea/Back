package org.ozea.point.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.point.dto.PointDTO;
import org.ozea.point.service.PointService;
import org.ozea.security.util.JwtProcessor;
import org.ozea.user.dto.UserDTO;
import org.ozea.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Point")
public class PointController {
    private final PointService pointService;
    private final JwtProcessor jwtProcessor;
    private final UserService userService;
    @GetMapping("/history/{userId}")
    @Operation(summary = "포인트 내역 조회", description = "사용자의 포인트 적립/출금 내역을 조회합니다.")
    public ResponseEntity<List<PointDTO>> getPointHistory(@PathVariable UUID userId) {
        List<PointDTO> history = pointService.getPointHistory(userId);
        return ResponseEntity.ok(history);
    }
    @GetMapping("/history/{userId}/{type}")
    @Operation(summary = "포인트 내역 조회 (타입별)", description = "사용자의 포인트 내역을 타입별로 조회합니다.")
    public ResponseEntity<List<PointDTO>> getPointHistoryByType(
            @PathVariable UUID userId,
            @PathVariable String type) {
        List<PointDTO> history = pointService.getPointHistoryByType(userId, type);
        return ResponseEntity.ok(history);
    }
    @GetMapping("/total/{userId}")
    @Operation(summary = "총 포인트 조회", description = "사용자의 총 보유 포인트를 조회합니다.")
    public ResponseEntity<Map<String, Integer>> getTotalPoints(@PathVariable UUID userId) {
        Integer totalPoints = pointService.getTotalPoints(userId);
        return ResponseEntity.ok(Map.of("totalPoints", totalPoints));
    }
    @PostMapping("/add/{userId}")
    @Operation(summary = "포인트 적립", description = "사용자에게 포인트를 적립합니다.")
    public ResponseEntity<Map<String, String>> addPoints(
            @PathVariable UUID userId,
            @RequestBody Map<String, Object> request) {
        Integer amount = (Integer) request.get("amount");
        String reason = (String) request.get("reason");
        pointService.addPoints(userId, amount, reason);
        return ResponseEntity.ok(Map.of("message", "포인트가 성공적으로 적립되었습니다."));
    }
    @PostMapping("/withdraw")
    @Operation(summary = "포인트 출금", description = "현재 로그인한 사용자의 포인트를 출금합니다.")
    public ResponseEntity<Map<String, Object>> withdrawPoints(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtProcessor.getUsername(token);
            UserDTO user = userService.getUserByEmail(email);
            Integer amount = (Integer) request.get("amount");
            String reason = (String) request.get("reason");
            pointService.withdrawPoints(UUID.fromString(user.getUserId()), amount, reason);
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "포인트가 성공적으로 출금되었습니다."
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}