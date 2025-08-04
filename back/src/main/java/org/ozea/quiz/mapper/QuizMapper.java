package org.ozea.quiz.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ozea.quiz.dto.QuizDTO;

import java.util.Optional;

@Mapper
public interface QuizMapper {

    // 랜덤으로 퀴즈 하나 조회
    QuizDTO findRandomQuiz();

    // 퀴즈 ID로 조회
    QuizDTO findById(@Param("quizId") int quizId);

    // 사용자 퀴즈 답안 결과 저장
    void saveUserQuizResult(
            @Param("userId") String userId,
            @Param("quizId") int quizId,
            @Param("isCorrect") boolean isCorrect
    );

    //사용자가 오늘 이미 퀴즈를 풀었는지 확인
    boolean existsTodayQuizByUserId(@Param("userId") String userId);
}
