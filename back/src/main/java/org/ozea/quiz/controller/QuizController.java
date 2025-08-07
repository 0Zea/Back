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
    @GetMapping("/today")
    public ResponseEntity<QuizResponseDTO> getTodayQuiz(@RequestParam String userId) {
        try {
            validateUserId(userId);
            QuizResponseDTO response = quizService.getTodayQuiz(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    @PostMapping("/submit")
    public ResponseEntity<QuizSubmitResponseDTO> submitAnswer(
            @RequestParam String userId,
            @RequestBody QuizSubmitRequestDTO request) {
        try {
            validateUserId(userId);
            QuizSubmitResponseDTO response = quizService.submitAnswer(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(null);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("존재하지 않는 퀴즈")) {
                return ResponseEntity.status(404).body(null);
            }
            return ResponseEntity.status(500).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        if (userId.length() < 10) {
            throw new IllegalArgumentException("유효하지 않은 userId 형식입니다.");
        }
    }
}
