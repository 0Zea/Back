package org.ozea.quiz.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 퀴즈 정답 확인 API 응답 DTO
 * - 정답 확인 결과를 클라이언트로 전송 (간단한 구조)
 */
@Getter
@Builder
public class QuizSubmitResponseDTO {

    private boolean isCorrect;
    private int quiz_id;

    /**
     * 정답/오답 결과 생성
     * @param isCorrect 정답 여부
     * @param quiz_id 퀴즈 ID
     * @return QuizSubmitResponseDTO
     */
    public static QuizSubmitResponseDTO of(boolean isCorrect, int quiz_id) {
        return QuizSubmitResponseDTO.builder()
                .isCorrect(isCorrect)
                .quiz_id(quiz_id)
                .build();
    }
}