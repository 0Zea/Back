package org.ozea.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.ozea.security.account.dto.LoginDTO;
import org.ozea.security.handler.LoginFailureHandler;
import org.ozea.security.handler.LoginSuccessHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Log4j2
public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    // 스프링 생성자 주입을 통해 전달
    public JwtUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager, // SecurityConfig가 생성된 이후에 등록됨
                                                   LoginSuccessHandler loginSuccessHandler,
                                                   LoginFailureHandler loginFailureHandler) {
        super(authenticationManager);
        setFilterProcessesUrl("/api/auth/login"); // POST 로그인 요청 url
        setAuthenticationSuccessHandler(loginSuccessHandler); // 로그인 성공 핸들러 등록
        setAuthenticationFailureHandler(loginFailureHandler); // 로그인 실패 핸들러 등록
    }

    // 로그인 요청 URL인 경우 로그인 작업 처리
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        log.info("🔥 JwtUsernamePasswordAuthenticationFilter 실행됨");

        try {
            // 🔥 JSON Body를 문자열로 먼저 읽어서 로그로 확인
            String body = new BufferedReader(new InputStreamReader(request.getInputStream()))
                    .lines()
                    .reduce("", (acc, line) -> acc + line);

            log.info("📦 Request Body: {}", body);

            // 🔍 ObjectMapper로 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            LoginDTO login = objectMapper.readValue(body, LoginDTO.class);

            log.info("✅ 파싱된 로그인 정보: email={}, password={}", login.getEmail(), login.getPassword());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword());

            return getAuthenticationManager().authenticate(authToken);

        } catch (Exception e) {
            log.error("❌ 로그인 요청 파싱 실패", e);
            throw new RuntimeException("로그인 요청 형식이 잘못되었습니다.");
        }
    }

}