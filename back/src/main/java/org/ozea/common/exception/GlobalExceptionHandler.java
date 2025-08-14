package org.ozea.common.exception;

import lombok.extern.log4j.Log4j2;
import org.ozea.common.dto.ApiResponse;
import org.ozea.quiz.exception.AlreadySolvedException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import javax.validation.ConstraintViolationException;
import java.util.NoSuchElementException;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* 400 - 잘못된 요청 (비즈니스/검증) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("400 Bad Request (IllegalArgument): {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<?> handleBadRequest(Exception e) {
        log.warn("400 Bad Request ({}): {}", e.getClass().getSimpleName(), e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST, "요청 파라미터가 올바르지 않습니다."));
    }

    /* 401 - 인증 실패 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException e) {
        log.warn("401 Unauthorized: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "인증에 실패했습니다."));
    }

    /* 403 - 권한 없음 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("403 Forbidden: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCode.FORBIDDEN, "접근 권한이 없습니다."));
    }

    /* 404 - 리소스 없음 (필요 시 사용) */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNotFound(NoSuchElementException e) {
        log.warn("404 Not Found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."));
    }

    /* 409 - 무결성 위반 & 비즈니스 충돌 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("409 Conflict (DataIntegrity): {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.DATA_INTEGRITY_ERROR, "데이터 처리 중 오류가 발생했습니다."));
    }

    /* 예: 이미 오늘 퀴즈를 풀었을 때 같은 도메인 충돌 예외를 409로 */
    @ExceptionHandler(AlreadySolvedException.class)
    public ResponseEntity<?> handleAlreadySolved(AlreadySolvedException e) {
        log.warn("409 Conflict (AlreadySolved): {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.CONFLICT, e.getMessage()));
    }

    /* 500 - DB 접근 일반 오류 */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> handleDataAccess(DataAccessException e) {
        log.error("500 Internal Server Error (DataAccess)", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "DB 처리 중 문제가 발생했습니다."));
    }

    /* 500 - 런타임 기타 (가급적 마지막에서 처리) */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        log.error("500 Internal Server Error (Runtime)", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."));
    }

    /* 500 - 최종 캐치(정말 마지막 하나만) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAny(Exception e) {
        log.error("500 Internal Server Error (Unhandled)", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."));
    }
}