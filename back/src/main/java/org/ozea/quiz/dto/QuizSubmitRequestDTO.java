package org.ozea.quiz.dto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor
public class QuizSubmitRequestDTO {
    private Integer quiz_id;
    private String user_answer;
    @Builder
    public QuizSubmitRequestDTO(Integer quiz_id, String user_answer) {
        this.quiz_id = quiz_id;
        this.user_answer = user_answer;
    }
}