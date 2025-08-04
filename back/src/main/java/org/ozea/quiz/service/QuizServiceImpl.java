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
        if (quizMapper.existsTodayQuizByUserId(userId)) {
            throw new IllegalStateException("오늘은 이미 퀴즈를 풀었습니다. 내일 다시 도전해보세요!");
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
