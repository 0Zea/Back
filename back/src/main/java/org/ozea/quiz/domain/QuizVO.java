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
    private String explanation;
    private String quiz_type;
    public boolean isCorrectAnswer(String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }
        String cleanUserAnswer = userAnswer.trim();
        if ("OX".equalsIgnoreCase(this.quiz_type)) {
            return this.answer.equalsIgnoreCase(cleanUserAnswer);
        } else if ("short".equalsIgnoreCase(this.quiz_type)) {
            return this.answer.toLowerCase().trim()
                    .equals(cleanUserAnswer.toLowerCase());
        }
        return false;
    }
    public boolean isOXQuiz() {
        return "OX".equalsIgnoreCase(this.quiz_type);
    }
    public boolean isShortAnswerQuiz() {
        return "short".equalsIgnoreCase(this.quiz_type);
    }
    public String getApiQuizType() {
        if ("OX".equalsIgnoreCase(this.quiz_type)) {
            return "OX";
        } else if ("short".equalsIgnoreCase(this.quiz_type)) {
            return "short";
        }
        return this.quiz_type.toLowerCase();
    }
    public boolean isValidQuestion() {
        return this.question != null &&
                !this.question.trim().isEmpty() &&
                this.question.trim().length() <= 255;
    }
    public boolean isValidAnswer() {
        return this.answer != null &&
                !this.answer.trim().isEmpty() &&
                this.answer.trim().length() <= 50;
    }
    public boolean isValid() {
        return isValidQuestion() &&
                isValidAnswer() &&
                (isOXQuiz() || isShortAnswerQuiz());
    }
}