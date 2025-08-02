package org.ozea.quiz.dto;

import org.ozea.quiz.domain.QuizVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDTO {

    private int quiz_id;
    private String question;
    private String answer;
    private String quiz_type;

    // QuizVO를 QuizDTO로 변환s
    public static QuizDTO of(QuizVO quizVO) {
        return QuizDTO.builder()
                .quiz_id(quizVO.getQuiz_id())
                .question(quizVO.getQuestion())
                .answer(quizVO.getAnswer())
                .quiz_type(quizVO.getQuiz_type())
                .build();
    }


    // QuizDTO를 QuizVO로 변환
    public QuizVO toVO() {
        return QuizVO.builder()
                .quiz_id(this.quiz_id)
                .question(this.question)
                .answer(this.answer)
                .quiz_type(this.quiz_type)
                .build();
    }
}