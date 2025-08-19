package org.ozea.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ozea.common.dto.ApiResponse;
import org.ozea.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonResponse {

    private final ObjectMapper objectMapper;

    public <T> void send(HttpServletResponse res, T body) throws IOException {
        send(res, HttpStatus.OK, ApiResponse.success(body));
    }

    public <T> void send(HttpServletResponse res, HttpStatus status, T body) throws IOException {
        res.setStatus(status.value());
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(objectMapper.writeValueAsString(body));
        res.getWriter().flush();
    }

    public void sendError(HttpServletResponse res, HttpStatus status, String message) throws IOException {
        res.setStatus(status.value());
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(message)));
        res.getWriter().flush();
    }

    public void sendError(HttpServletResponse res, ErrorCode errorCode) throws IOException {
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(errorCode)));
        res.getWriter().flush();
    }

    public void sendError(HttpServletResponse res, HttpStatus status, ErrorCode errorCode, String message) throws IOException {
        res.setStatus(status.value());
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(errorCode, message)));
        res.getWriter().flush();
    }
}