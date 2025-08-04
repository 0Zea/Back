package org.ozea.quiz.dto;

import lombok.Builder;
import lombok.Getter;
import org.ozea.quiz.domain.QuizVO;

/**
 * 퀴즈 조회 API 응답 DTO
 * - 클라이언트에게 문제만 전송 (정답 숨김)
 */
@Getter
@Builder
public class QuizResponseDTO {

    private int quiz_id;
    private String question;
    private String type; // "OX" 또는 "short"

    /**
     * QuizVO를 API 응답 DTO로 변환 (정답 제외)
     */
    public static QuizResponseDTO of(QuizVO quizVO) {
        return QuizResponseDTO.builder()
                .quiz_id(quizVO.getQuiz_id())
                .question(quizVO.getQuestion())
                .type(quizVO.getApiQuizType())
                .build();
    }

    /**
     * QuizDTO를 API 응답 DTO로 변환 (정답 제외)
     */
    public static QuizResponseDTO of(QuizDTO quizDTO) {
        return of(quizDTO.toVO());
    }
}
