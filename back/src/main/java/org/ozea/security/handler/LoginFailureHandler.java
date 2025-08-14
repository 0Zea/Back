package org.ozea.security.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.util.JsonResponse;
import org.ozea.security.filter.JwtUsernamePasswordAuthenticationFilter;
import org.ozea.security.service.LoginAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Log4j2
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;
    private final JsonResponse jsonResponse;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        
        Object emailAttr = request.getAttribute(JwtUsernamePasswordAuthenticationFilter.REQ_LOGIN_EMAIL);
        String email = (emailAttr instanceof String) ? (String) emailAttr : request.getParameter("email");

        if (email != null) {
            loginAttemptService.recordFailedAttempt(email);

            if (loginAttemptService.isBlocked(email)) {
                log.warn("잠긴 계정으로 로그인 시도: {}", email);
                jsonResponse.sendError(response, HttpStatus.TOO_MANY_REQUESTS,
                        "계정이 잠겼습니다. 5분 후 다시 시도해주세요.");
                return;
            }

            int remaining = loginAttemptService.getRemainingAttempts(email);
            if (remaining <= 2) {
                log.warn("로그인 실패 (남은 시도: {}): {}", remaining, email);
                jsonResponse.sendError(response, HttpStatus.UNAUTHORIZED,
                        String.format("로그인에 실패했습니다. 남은 시도 횟수: %d", remaining));
                return;
            }
        }

        log.warn("로그인 실패: {} - {}", email, exception.getMessage());
        jsonResponse.sendError(response, HttpStatus.UNAUTHORIZED,
                "사용자 이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}