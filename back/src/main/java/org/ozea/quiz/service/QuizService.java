package org.ozea.quiz.service;

import org.apache.ibatis.annotations.Param;
import org.ozea.quiz.dto.QuizResponseDTO;
import org.ozea.quiz.dto.QuizSubmitRequestDTO;
import org.ozea.quiz.dto.QuizSubmitResponseDTO;

public interface QuizService {

    QuizResponseDTO getTodayQuiz(@Param("userId") String userId);
    QuizSubmitResponseDTO submitAnswer(String userId, QuizSubmitRequestDTO request);
}
