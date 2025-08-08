package org.ozea.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolvedQuizResponseDTO {

    private String question;
    private String explanation;
    private boolean isCorrect;

    public static SolvedQuizResponseDTO of(String question, String explanation, boolean isCorrect) {
        return SolvedQuizResponseDTO.builder()
                .question(question)
                .explanation(explanation)
                .isCorrect(isCorrect)
                .build();
    }

//    public boolean isAlreadySolved() {
//        return true;
//    }
}