package org.ozea.quiz.domain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuizVO {
    private String user_quiz_id;
    private String user_id;
    private int quiz_id;
    private boolean is_correct;
    private LocalDateTime answered_at;
}
