package org.ozea.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 사용자 관련 에러
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS("U002", "이미 존재하는 사용자입니다."),
    INVALID_PASSWORD("U003", "잘못된 비밀번호입니다."),
    EMAIL_ALREADY_EXISTS("U004", "이미 사용 중인 이메일입니다."),
    // 인증 관련 에러
    UNAUTHORIZED("A001", "인증이 필요합니다."),
    INVALID_TOKEN("A002", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED("A003", "토큰이 만료되었습니다."),
    ACCESS_DENIED("A004", "접근 권한이 없습니다."),
    // 목표 관련 에러
    GOAL_NOT_FOUND("G001", "목표를 찾을 수 없습니다."),
    GOAL_ALREADY_EXISTS("G002", "이미 존재하는 목표입니다."),
    INVALID_GOAL_AMOUNT("G003", "유효하지 않은 목표 금액입니다."),
    // 자산 관련 에러
    ASSET_NOT_FOUND("AS001", "자산을 찾을 수 없습니다."),
    INVALID_ASSET_AMOUNT("AS002", "유효하지 않은 자산 금액입니다."),
    // 공지사항 관련 에러
    NOTICE_NOT_FOUND("N001", "공지사항을 찾을 수 없습니다."),
    // 문의 관련 에러
    INQUIRY_NOT_FOUND("I001", "문의를 찾을 수 없습니다."),
    // 시스템 에러
    INTERNAL_SERVER_ERROR("S001", "내부 서버 오류가 발생했습니다."),
    EXTERNAL_API_ERROR("S002", "외부 API 호출 중 오류가 발생했습니다."),
    INVALID_INPUT("C001", "입력값이 유효하지 않습니다."),
    DATABASE_ERROR("S003", "데이터베이스 오류가 발생했습니다."),
    TOO_MANY_REQUESTS("C002", "너무 많은 요청입니다."), BAD_REQUEST("G001", "오류 발생"),
    DATA_INTEGRITY_ERROR("G002", "데이터 오류"),
    FORBIDDEN("G003", "접근권한 없음"),
    CONFLICT("G005", "충돌발생"), NOT_FOUND("G008", "못찾음");
    private final String code;
    private final String message;

}