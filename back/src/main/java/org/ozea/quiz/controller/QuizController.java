package org.ozea.quiz.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.quiz.dto.QuizResponseDTO;
import org.ozea.quiz.dto.QuizSubmitRequestDTO;
import org.ozea.quiz.dto.QuizSubmitResponseDTO;
import org.ozea.quiz.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Log4j2
public class QuizController {

    private final QuizService quizService;

    // 오늘의 퀴즈 조회
    // GET /api/quiz/today?userId={userId}
    @GetMapping("/today")
    public ResponseEntity<QuizResponseDTO> getTodayQuiz(@RequestParam String userId) {
        log.info("오늘의 퀴즈 조회 API 호출");

        try {
            validateUserId(userId);
            QuizResponseDTO response = quizService.getTodayQuiz(userId);

            log.info("오늘의 퀴즈 조회 성공 - userId: {}, quizId: {}", userId, response.getQuiz_id());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("오늘의 퀴즈 조회 실패 - 잘못된 userId: {}", e.getMessage());
            return ResponseEntity.status(400) // 400 Bad Request
                    .body(null);

        } catch (IllegalStateException e) {
            log.warn("오늘의 퀴즈 조회 실패 - 이미 완료: {}", e.getMessage());
            return ResponseEntity.status(409) // 409 Conflict
                    .body(null);

        } catch (Exception e) {
            log.error("오늘의 퀴즈 조회 중 오류 발생", e);
            return ResponseEntity.status(500) // 500 Internal Server Error
                    .body(null);
        }
    }

    // 퀴즈 정답 제출
    // POST /api/quiz/submit?userId={userId}
    @PostMapping("/submit")
    public ResponseEntity<QuizSubmitResponseDTO> submitAnswer(
            @RequestParam String userId,
            @RequestBody QuizSubmitRequestDTO request) {
        log.info("퀴즈 정답 제출 API 호출 - userId: {}, quizId: {}", userId, request.getQuiz_id());

        try {
            // userId 유효성 검증
            validateUserId(userId);

            QuizSubmitResponseDTO response = quizService.submitAnswer(userId, request);

            log.info("퀴즈 정답 제출 성공 - userId: {}, quizId: {}, isCorrect: {}",
                    userId, response.getQuiz_id(), response.isCorrect());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("퀴즈 정답 제출 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(null);

        } catch (IllegalStateException e) {
            log.warn("퀴즈 정답 제출 실패 - 이미 완료: {}", e.getMessage());
            return ResponseEntity.status(409)
                    .body(null);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("존재하지 않는 퀴즈")) {
                log.warn("퀴즈 정답 제출 실패 - 퀴즈 없음: {}", e.getMessage());
                return ResponseEntity.status(404)
                        .body(null);
            }

            log.error("퀴즈 정답 제출 중 오류 발생", e);
            return ResponseEntity.status(500)
                    .body(null);

        } catch (Exception e) {
            log.error("퀴즈 정답 제출 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(500)
                    .body(null);
        }
    }

    // userId 유효성 검증
    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }

        // UUID 형식 간단 검증 (선택사항)
        if (userId.length() < 10) {
            throw new IllegalArgumentException("유효하지 않은 userId 형식입니다.");
        }

        log.debug("userId 유효성 검증 완료 - userId: {}", userId);
    }
}
