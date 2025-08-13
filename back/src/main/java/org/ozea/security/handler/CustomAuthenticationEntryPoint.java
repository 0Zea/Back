package org.ozea.security.handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.util.JsonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Log4j2
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonResponse jsonResponse;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        jsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, authException.getMessage());
    }
}
