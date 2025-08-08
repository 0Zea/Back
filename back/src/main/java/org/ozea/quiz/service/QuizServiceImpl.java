package org.ozea.quiz.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.point.service.PointService;
import org.ozea.quiz.dto.*;
import org.ozea.quiz.exception.AlreadySolvedException;
import org.ozea.quiz.mapper.QuizMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Log4j2
public class QuizServiceImpl implements QuizService {
    private final QuizMapper quizMapper;
    private final PointService pointService;
    @Override
    public QuizResponseDTO getTodayQuiz(String userId) {
        if (quizMapper.existsTodayQuizByUserId(userId)) {

            SolvedQuizResponseDTO solvedQuiz = quizMapper.getTodaySolvedQuizInfo(userId);
            if (solvedQuiz == null) {
                throw new IllegalStateException("오늘 푼 퀴즈 정보를 찾을 수 없습니다.");
            }
            throw new AlreadySolvedException(solvedQuiz);
        }
        QuizDTO quiz = quizMapper.findRandomQuiz();
        if (quiz == null) {
            throw new RuntimeException("퀴즈를 찾을 수 없습니다.");
        }
        return QuizResponseDTO.of(quiz.toVO());
    }
    @Override
    @Transactional
    public QuizSubmitResponseDTO submitAnswer(String userId, QuizSubmitRequestDTO request) {
        validateSubmitRequest(request);
        if (quizMapper.existsTodayQuizByUserId(userId)) {
            throw new IllegalStateException("오늘은 이미 퀴즈를 풀었습니다.");
        }
        QuizDTO quiz = quizMapper.findById(request.getQuiz_id());
        if (quiz == null) {
            throw new RuntimeException("존재하지 않는 퀴즈입니다. ID: " + request.getQuiz_id());
        }
        boolean isCorrect = quiz.toVO().isCorrectAnswer(request.getUser_answer());
        quizMapper.saveUserQuizResult(userId, request.getQuiz_id(), isCorrect);
        // 정답인 경우 포인트 지급
        if (isCorrect) {
            try {
                UUID userUUID = UUID.fromString(userId);
                pointService.addPoints(userUUID, 10, "퀴즈 정답 보상");
                log.info("퀴즈 정답 포인트 지급: userId={}, amount=10", userId);
            } catch (Exception e) {
                log.error("퀴즈 정답 포인트 지급 실패: userId={}, error={}", userId, e.getMessage());
                // 포인트 지급 실패해도 퀴즈 결과는 저장
            }
        }
        return QuizSubmitResponseDTO.of(isCorrect, request.getQuiz_id());
    }
    private void validateSubmitRequest(QuizSubmitRequestDTO request) {
        if (request.getQuiz_id() == null) {
            throw new IllegalArgumentException("퀴즈 ID는 필수입니다.");
        }
        if (request.getUser_answer() == null || request.getUser_answer().trim().isEmpty()) {
            throw new IllegalArgumentException("답안은 비어있을 수 없습니다.");
        }
    }
}
