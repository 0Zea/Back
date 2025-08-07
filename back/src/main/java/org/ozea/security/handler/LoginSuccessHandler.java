package org.ozea.security.handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.user.domain.User;
import org.ozea.security.account.domain.CustomUser;
import org.ozea.security.account.dto.AuthResultDTO;
import org.ozea.security.account.dto.UserInfoDTO;
import org.ozea.security.service.LoginAttemptService;
import org.ozea.security.util.JsonResponse;
import org.ozea.security.util.JwtProcessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Log4j2
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProcessor jwtProcessor;
    private final LoginAttemptService loginAttemptService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomUser customUser = (CustomUser) authentication.getPrincipal();
        String email = customUser.getUsername();
        // 로그인 성공 기록
        loginAttemptService.recordSuccessfulAttempt(email);
        // 신규 사용자 체크
        if (customUser.isNewUser()) {
            response.sendRedirect("/additional-info.jsp");
            return;
        }
        // Access Token과 Refresh Token 생성
        String accessToken = jwtProcessor.generateAccessToken(email);
        String refreshToken = jwtProcessor.generateRefreshToken(email);
        // 세션에 사용자 정보 저장
        request.getSession().setAttribute("nickname", customUser.getUser().getName());
        request.getSession().setAttribute("email", customUser.getUser().getEmail());
        // 응답 데이터 구성
        UserInfoDTO userInfo = UserInfoDTO.of(customUser.getUser());
        AuthResultDTO result = new AuthResultDTO(accessToken, refreshToken, userInfo, false);
        // 보안 헤더 설정
        response.setHeader("X-Auth-Token", accessToken);
        response.setHeader("X-Refresh-Token", refreshToken);
        log.info("로그인 성공: {}", email);
        JsonResponse.send(response, result);
    }
}
