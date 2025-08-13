package org.ozea.security.handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.security.service.LoginAttemptService;
import org.ozea.common.util.JsonResponse;
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
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("email");
        if (email != null) {
            // 로그인 실패 기록
            loginAttemptService.recordFailedAttempt(email);
            // 계정 잠금 상태 확인
            if (loginAttemptService.isBlocked(email)) {
                log.warn("잠긴 계정으로 로그인 시도: {}", email);
                jsonResponse.sendError(response, HttpStatus.TOO_MANY_REQUESTS,
                    "계정이 잠겼습니다. 5분 후 다시 시도해주세요.");
                return;
            }
            // 남은 시도 횟수 확인
            int remainingAttempts = loginAttemptService.getRemainingAttempts(email);
            if (remainingAttempts <= 2) {
                log.warn("로그인 실패 (남은 시도: {}): {}", remainingAttempts, email);
                jsonResponse.sendError(response, HttpStatus.UNAUTHORIZED,
                    String.format("로그인에 실패했습니다. 남은 시도 횟수: %d", remainingAttempts));
                return;
            }
        }
        log.warn("로그인 실패: {} - {}", email, exception.getMessage());
        jsonResponse.sendError(response, HttpStatus.UNAUTHORIZED,
            "사용자 이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
