package org.ozea.quiz.exception;

import org.ozea.quiz.dto.SolvedQuizResponseDTO;

public class AlreadySolvedException extends RuntimeException {

    private final SolvedQuizResponseDTO solvedQuizData;

    public AlreadySolvedException(SolvedQuizResponseDTO solvedQuizData) {
        super("이미 오늘의 퀴즈를 완료했습니다.");
        this.solvedQuizData = solvedQuizData;
    }

    public AlreadySolvedException(String message, SolvedQuizResponseDTO solvedQuizData) {
        super(message);
        this.solvedQuizData = solvedQuizData;
    }

    public SolvedQuizResponseDTO getSolvedQuizData() {
        return solvedQuizData;
    }
}