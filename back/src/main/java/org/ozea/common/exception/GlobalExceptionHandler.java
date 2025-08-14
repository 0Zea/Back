package org.ozea.common.exception;

import lombok.extern.log4j.Log4j2;
import org.ozea.common.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - 잘못된 요청 (비즈니스/검증)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("IllegalArgument: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    // 400 - 런타임(원래 있던 Map 포맷 유지가 필요하면 아래 주석 해제하고 ApiResponse 대신 Map 반환)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        log.warn("RuntimeException: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST, e.getMessage()));
        /*
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", e.getMessage());
        response.put("error", "RUNTIME_ERROR");
        return ResponseEntity.badRequest().body(response);
        */
    }

    // 409 - 무결성 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolation: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.DATA_INTEGRITY_ERROR, "데이터 처리 중 오류가 발생했습니다."));
        /*
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "데이터 처리 중 오류가 발생했습니다.");
        response.put("error", "DATA_INTEGRITY_ERROR");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        */
    }

    // 401 - 인증 실패
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException e) {
        log.warn("AuthenticationException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "인증에 실패했습니다."));
        /*
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "인증에 실패했습니다.");
        response.put("error", "AUTHENTICATION_ERROR");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        */
    }

    // 403 - 권한 없음
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("AccessDeniedException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCode.FORBIDDEN, "접근 권한이 없습니다."));
        /*
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "접근 권한이 없습니다.");
        response.put("error", "ACCESS_DENIED");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        */
    }

    // ✅ 마지막 ‘모든 예외’는 오직 하나만 둔다 (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAny(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."));
        /*
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "서버 오류가 발생했습니다.");
        response.put("error", "INTERNAL_SERVER_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        */
    }
}