package org.ozea.security.handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.ozea.security.account.domain.CustomUser;
import org.ozea.security.account.dto.AuthResultDTO;
import org.ozea.security.account.dto.UserInfoDTO;
import org.ozea.security.service.LoginAttemptService;
import org.ozea.common.util.JsonResponse;
import org.ozea.security.util.JwtProcessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Log4j2
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProcessor jwtProcessor;
    private final LoginAttemptService loginAttemptService;
    private final JsonResponse jsonResponse;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomUser customUser = (CustomUser) authentication.getPrincipal();
        String email = customUser.getUsername();
        loginAttemptService.recordSuccessfulAttempt(email);
        if (customUser.isNewUser()) {
            response.sendRedirect("/additional-info.jsp");
            return;
        }

        String accessToken = jwtProcessor.generateAccessToken(email);
        String refreshToken = jwtProcessor.generateRefreshToken(email);

        request.getSession().setAttribute("nickname", customUser.getUser().getName());
        request.getSession().setAttribute("email", customUser.getUser().getEmail());

        UserInfoDTO userInfo = UserInfoDTO.of(customUser.getUser());
        AuthResultDTO result = new AuthResultDTO(accessToken, refreshToken, userInfo, false);

        response.setHeader("X-Auth-Token", accessToken);
        response.setHeader("X-Refresh-Token", refreshToken);
        log.info("로그인 성공: {}", email);
        jsonResponse.send(response, result);
    }
}
