package org.ozea.quiz.dto;
import lombok.Builder;
import lombok.Getter;
@Getter
@Builder
public class QuizSubmitResponseDTO {
    private boolean isCorrect;
    private int quiz_id;
    public static QuizSubmitResponseDTO of(boolean isCorrect, int quiz_id) {
        return QuizSubmitResponseDTO.builder()
                .isCorrect(isCorrect)
                .quiz_id(quiz_id)
                .build();
    }
}