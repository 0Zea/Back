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

    /**
     * 로그인 성공 시 호출되는 메서드
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 인증된 사용자 정보 꺼내기
        CustomUser customUser = (CustomUser) authentication.getPrincipal();

        // 신규 사용자인 경우 추가 정보 입력 페이지로 리디렉션
        if (customUser.isNewUser()) {
            response.sendRedirect("/additional-info.jsp"); // 또는 리디렉션할 경로
            return; // 리디렉션 후에는 추가 로직을 실행하지 않도록 종료
        }

        // JWT 토큰 생성
        String token = jwtProcessor.generateToken(customUser.getUsername());

        // 세션에 사용자 정보 저장
        request.getSession().setAttribute("nickname", customUser.getUser().getName());
        request.getSession().setAttribute("email", customUser.getUser().getEmail());

        // 사용자 정보 DTO 구성
        UserInfoDTO userInfo = UserInfoDTO.of(customUser.getUser());

        // 토큰 + 사용자 정보 DTO 반환
        AuthResultDTO result = new AuthResultDTO(token, userInfo, false);

        // JSON 응답 전송
        JsonResponse.send(response, result);
    }
}
