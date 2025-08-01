package org.ozea.quiz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.quiz.dto.QuizDTO;
import org.ozea.quiz.dto.QuizResponseDTO;
import org.ozea.quiz.dto.QuizSubmitRequestDTO;
import org.ozea.quiz.dto.QuizSubmitResponseDTO;
import org.ozea.quiz.mapper.QuizMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class QuizServiceImpl implements QuizService {

    private final QuizMapper quizMapper;

    @Override
    public QuizResponseDTO getTodayQuiz(String userId) {
        log.debug("오늘의 퀴즈 조회 시작 - userId: {}", userId);

        // 1. 오늘 이미 퀴즈를 풀었는지 확인
        if (quizMapper.existsTodayQuizByUserId(userId)) {
            log.warn("사용자가 오늘 이미 퀴즈를 풀었습니다 - userId: {}", userId);
            throw new IllegalStateException("오늘은 이미 퀴즈를 풀었습니다. 내일 다시 도전해보세요!");
        }

        // 2. 랜덤 퀴즈 조회
        QuizDTO quiz = quizMapper.findRandomQuiz();

        if (quiz == null) {
            throw new RuntimeException("퀴즈를 찾을 수 없습니다.");
        }


        log.debug("오늘의 퀴즈 조회 완료 - quizId: {}, type: {}", quiz.getQuiz_id(), quiz.getQuiz_type());

        // 3. 응답 DTO 변환 (정답 제외)
        return QuizResponseDTO.of(quiz.toVO());
    }

    @Override
    @Transactional
    public QuizSubmitResponseDTO submitAnswer(String userId, QuizSubmitRequestDTO request) {
        log.debug("퀴즈 정답 제출 시작 - userId: {}, quizId: {}, userAnswer: {}",
                userId, request.getQuiz_id(), request.getUser_answer());

        log.debug("⭐ submitAnswer 메서드에서 확인된 user_answer: [{}]", request.getUser_answer());

        // 1. 요청 데이터 유효성 검증
        validateSubmitRequest(request);

        // 2. 오늘 이미 퀴즈를 풀었는지 재확인
        if (quizMapper.existsTodayQuizByUserId(userId)) {
            log.warn("사용자가 오늘 이미 퀴즈를 풀었습니다 - userId: {}", userId);
            throw new IllegalStateException("오늘은 이미 퀴즈를 풀었습니다.");
        }

        // 3. 퀴즈 정보 조회 (정답 확인용)
        QuizDTO quiz = quizMapper.findById(request.getQuiz_id());

        if (quiz == null) {
            throw new RuntimeException("존재하지 않는 퀴즈입니다. ID: " + request.getQuiz_id());
        }

        // 4. 정답 확인
        boolean isCorrect = quiz.toVO().isCorrectAnswer(request.getUser_answer());

        log.info("정답 확인 완료 - quizId: {}, isCorrect: {}", request.getQuiz_id(), isCorrect);

        // 5. 결과 저장
        quizMapper.saveUserQuizResult(userId, request.getQuiz_id(), isCorrect);

        log.info("퀴즈 결과 저장 완료 - userId: {}, quizId: {}, isCorrect: {}",
                userId, request.getQuiz_id(), isCorrect);

        // 6. 응답 반환
        return QuizSubmitResponseDTO.of(isCorrect, request.getQuiz_id());

    }


    // 퀴즈 제출 요청 유효성 검증
    private void validateSubmitRequest(QuizSubmitRequestDTO request) {
        if (request.getQuiz_id() == null) {
            throw new IllegalArgumentException("퀴즈 ID는 필수입니다.");
        }

        if (request.getUser_answer() == null || request.getUser_answer().trim().isEmpty()) {
            throw new IllegalArgumentException("답안은 비어있을 수 없습니다.");
        }

        log.debug("요청 데이터 유효성 검증 완료 - quizId: {}", request.getQuiz_id());
    }
}
