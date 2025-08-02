package org.ozea.quiz.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizVO {

    private int quiz_id;
    private String question;
    private String answer;
    private String quiz_type;

    /**
     * 정답 확인 비즈니스 로직
     * @param userAnswer 사용자가 입력한 답
     * @return 정답 여부
     */
    public boolean isCorrectAnswer(String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }

        String cleanUserAnswer = userAnswer.trim();

        if ("OX".equalsIgnoreCase(this.quiz_type)) {
            // O/X 퀴즈: 정확히 일치 (대소문자 무시)
            return this.answer.equalsIgnoreCase(cleanUserAnswer);
        } else if ("short".equalsIgnoreCase(this.quiz_type)) {
            // 단답형: 대소문자 무시, 공백 제거하여 비교
            return this.answer.toLowerCase().trim()
                    .equals(cleanUserAnswer.toLowerCase());
        }

        return false;
    }

    /**
     * O/X 퀴즈인지 확인
     */
    public boolean isOXQuiz() {
        return "OX".equalsIgnoreCase(this.quiz_type);
    }

    /**
     * 단답형 퀴즈인지 확인
     */
    public boolean isShortAnswerQuiz() {
        return "short".equalsIgnoreCase(this.quiz_type);
    }

    /**
     * 퀴즈 타입을 API 형식으로 변환
     * DB: "OX" -> API: "ox"
     * DB: "short" -> API: "short"
     */
    public String getApiQuizType() {
        if ("OX".equalsIgnoreCase(this.quiz_type)) {
            return "OX";
        } else if ("short".equalsIgnoreCase(this.quiz_type)) {
            return "short";
        }
        return this.quiz_type.toLowerCase();
    }

    /**
     * 문제 유효성 검증
     */
    public boolean isValidQuestion() {
        return this.question != null &&
                !this.question.trim().isEmpty() &&
                this.question.trim().length() <= 255;
    }

    /**
     * 정답 유효성 검증
     */
    public boolean isValidAnswer() {
        return this.answer != null &&
                !this.answer.trim().isEmpty() &&
                this.answer.trim().length() <= 50;
    }

    /**
     * 퀴즈 전체 유효성 검증
     */
    public boolean isValid() {
        return isValidQuestion() &&
                isValidAnswer() &&
                (isOXQuiz() || isShortAnswerQuiz());
    }
}