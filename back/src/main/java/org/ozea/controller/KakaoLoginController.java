package org.ozea.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.ozea.security.config.KakaoUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.*;
import java.util.*;

/**
 * 카카오 로그인을 처리하는 컨트롤러입니다.
 */
@Controller
public class KakaoLoginController {

    @Autowired
    private KakaoUserDetailsService kakaoUserDetailsService;

    @Value("${kakao.api.key}")
    private String REST_API_KEY; // 카카오 REST API 키

    @Value("${kakao.redirect.uri}")
    private String REDIRECT_URI; // 카카오 로그인 후 리다이렉트될 URI

    /**
     * 카카오 로그인 후 리다이렉트되는 콜백 요청을 처리합니다.
     * @param code 카카오에서 발급하는 인증 코드
     * @param session 현재 세션
     * @return "redirect:/main" - 메인 페이지로 리다이렉트
     * @throws Exception 예외 발생 시
     */
    @RequestMapping("/callback")
    public String kakaoCallback(@RequestParam("code") String code, HttpSession session) throws Exception {
        String accessToken = getAccessToken(code); // 인증 코드로 액세스 토큰을 발급받습니다.
        Map<String, Object> userInfo = getUserInfo(accessToken); // 액세스 토큰으로 사용자 정보를 조회합니다.

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        if (kakaoAccount == null) {
            throw new IllegalStateException("kakao_account is null. userInfo: " + userInfo);
        }

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) {
            throw new IllegalStateException("profile is null. kakaoAccount: " + kakaoAccount);
        }

        String email = (String) kakaoAccount.get("email");
        if (email == null || email.isEmpty()) {
            // 이메일 정보가 없는 경우, 닉네임을 기반으로 임시 이메일을 생성합니다.
            // 이메일 동의를 하지 않은 사용자의 로그인을 허용하기 위한 처리입니다.
            email = UUID.randomUUID().toString() + "@noemail.kakao";
        }

        String nickname = (String) profile.get("nickname");
        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalStateException("nickname is missing. profile: " + profile);
        }

        // Spring Security 인증 처리를 수행합니다.
        UserDetails userDetails = kakaoUserDetailsService.loadUserByUsername(email, nickname);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        session.setAttribute("email", email);
        session.setAttribute("nickname", nickname);

        // 추가 정보가 없으면 추가 정보 입력 페이지로 리다이렉트
        // (UserMapper에서 다시 조회)
        org.ozea.domain.User user = kakaoUserDetailsService.getUserByEmail(email);
        if (user.getRole() == null) {
            return "redirect:/additional-info";
        }

        return "redirect:/main";
    }

    /**
     * 카카오 인증 코드를 사용하여 액세스 토큰을 발급받습니다.
     * @param code 카카오에서 발급하는 인증 코드
     * @return 액세스 토큰
     * @throws Exception 예외 발생 시
     */
    private String getAccessToken(String code) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("https://kauth.kakao.com/oauth/token");

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("client_id", REST_API_KEY));
        params.add(new BasicNameValuePair("redirect_uri", REDIRECT_URI));
        params.add(new BasicNameValuePair("code", code));

        post.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response = client.execute(post);
        String body = EntityUtils.toString(response.getEntity());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonMap = mapper.readValue(body, Map.class);

        return (String) jsonMap.get("access_token");
    }

    /**
     * 액세스 토큰을 사용하여 카카오 사용자 정보를 조회합니다.
     * @param accessToken 액세스 토큰
     * @return 사용자 정보가 담긴 Map
     * @throws Exception 예외 발생 시
     */
    private Map<String, Object> getUserInfo(String accessToken) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet("https://kapi.kakao.com/v2/user/me");
        get.setHeader("Authorization", "Bearer " + accessToken);

        HttpResponse response = client.execute(get);
        String body = EntityUtils.toString(response.getEntity());

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(body, Map.class);
    }
}
