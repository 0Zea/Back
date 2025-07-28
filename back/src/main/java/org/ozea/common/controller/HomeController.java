package org.ozea.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import org.ozea.user.mapper.UserMapper;
import org.ozea.user.domain.User;

/**
 * 홈 및 로그인 페이지와 관련된 요청을 처리하는 컨트롤러입니다.
 */
@Controller
@Slf4j
public class HomeController {

    private final UserMapper userMapper;

    public HomeController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 루트 경로("/") 요청을 처리하여 인덱스 페이지를 반환합니다.
     * @return "index" - 인덱스 페이지의 뷰 이름
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // 회원가입
    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup"; // /WEB-INF/views/signup.jsp 로 포워딩됨 (viewResolver 사용 시)
    }

    // 회원가입 폼 제출 처리
    @PostMapping("/signup")
    public String processSignup(HttpServletRequest request) {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        User user = new User();
        user.setUserId(java.util.UUID.randomUUID());
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password); // 실제 서비스에서는 암호화 필요
        user.setMbti("미입력");
        user.setPhoneNum("000-0000-0000");
        user.setBirthDate(java.time.LocalDate.of(1900, 1, 1));
        user.setSex("female");
        user.setSalary(0L);
        user.setPayAmount(0L);
        user.setRole("USER");
        userMapper.insertUser(user);
        // 세션에 email 저장
        request.getSession().setAttribute("email", email);
        return "redirect:/additional-info";
    }

    // 로컬 로그인
    @GetMapping("/local-login")
    public String showLocalLoginPage() {
        return "local-login"; // → 실제 파일: /WEB-INF/views/local-login.jsp
    }

    /**
     * 메인 페이지("/main") 요청을 처리하여 메인 페이지를 반환합니다.
     * @return "main" - 메인 페이지의 뷰 이름
     */
    @GetMapping("/main")
    public String mainPage() {
        return "main";
    }
} 