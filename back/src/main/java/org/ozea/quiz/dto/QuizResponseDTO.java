package org.ozea.quiz.dto;
import lombok.Builder;
import lombok.Getter;
import org.ozea.quiz.domain.QuizVO;
@Getter
@Builder
public class QuizResponseDTO {
    private int quiz_id;
    private String question;
    private String type; // "OX" 또는 "short"
    public static QuizResponseDTO of(QuizVO quizVO) {
        return QuizResponseDTO.builder()
                .quiz_id(quizVO.getQuiz_id())
                .question(quizVO.getQuestion())
                .type(quizVO.getApiQuizType())
                .build();
    }
    public static QuizResponseDTO of(QuizDTO quizDTO) {
        return of(quizDTO.toVO());
    }
}
