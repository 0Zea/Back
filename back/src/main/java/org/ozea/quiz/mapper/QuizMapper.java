package org.ozea.quiz.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ozea.quiz.dto.QuizDTO;
import org.ozea.quiz.dto.SolvedQuizResponseDTO;

import java.util.Optional;
@Mapper
public interface QuizMapper {
    QuizDTO findRandomQuiz();
    QuizDTO findById(@Param("quizId") int quizId);
    void saveUserQuizResult(
            @Param("userId") String userId,
            @Param("quizId") int quizId,
            @Param("isCorrect") boolean isCorrect
    );
    boolean existsTodayQuizByUserId(@Param("userId") String userId);

    SolvedQuizResponseDTO getTodaySolvedQuizInfo(@Param("userId") String userId);
}
