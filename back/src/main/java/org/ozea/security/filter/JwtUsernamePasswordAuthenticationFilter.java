package org.ozea.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.ozea.security.account.dto.LoginDTO;
import org.ozea.security.handler.LoginFailureHandler;
import org.ozea.security.handler.LoginSuccessHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Log4j2
public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public static final String REQ_LOGIN_EMAIL = "REQ_LOGIN_EMAIL";

    private final ObjectMapper objectMapper;

    public JwtUsernamePasswordAuthenticationFilter(
            AuthenticationManager authenticationManager,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler,
            ObjectMapper objectMapper
    ) {
        super(authenticationManager);
        this.objectMapper = objectMapper;
        setFilterProcessesUrl("/api/auth/login");
        setAuthenticationSuccessHandler(loginSuccessHandler);
        setAuthenticationFailureHandler(loginFailureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            throw new AuthenticationServiceException("Content-Type must be application/json");
        }

        try {
            if (request.getCharacterEncoding() == null) {
                request.setCharacterEncoding("UTF-8");
            }
            LoginDTO login = objectMapper.readValue(request.getInputStream(), LoginDTO.class);
            if (login == null || login.getEmail() == null || login.getPassword() == null) {
                throw new AuthenticationServiceException("Invalid login payload");
            }

            // 실패 핸들러에서 쓸 수 있도록 요청 속성에 저장
            request.setAttribute(REQ_LOGIN_EMAIL, login.getEmail());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword());

            setDetails(request, authToken);
            return this.getAuthenticationManager().authenticate(authToken);

        } catch (Exception e) {
            log.warn("로그인 JSON 파싱 실패: {}", e.getMessage());
            throw new AuthenticationServiceException("로그인 요청 형식이 잘못되었습니다.", e);
        }
    }
}