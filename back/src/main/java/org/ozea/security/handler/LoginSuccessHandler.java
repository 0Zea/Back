package org.ozea.security.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.user.domain.User;
import org.ozea.security.account.domain.CustomUser;
import org.ozea.security.account.dto.AuthResultDTO;
import org.ozea.security.account.dto.UserInfoDTO;
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
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomUser customUser = (CustomUser) authentication.getPrincipal();

        if (customUser.isNewUser()) {
            response.sendRedirect("/additional-info.jsp");
            return;
        }

        String token = jwtProcessor.generateToken(customUser.getUsername());

        request.getSession().setAttribute("nickname", customUser.getUser().getName());
        request.getSession().setAttribute("email", customUser.getUser().getEmail());

        UserInfoDTO userInfo = UserInfoDTO.of(customUser.getUser());

        AuthResultDTO result = new AuthResultDTO(token, userInfo, false);

        JsonResponse.send(response, result);
    }
}
