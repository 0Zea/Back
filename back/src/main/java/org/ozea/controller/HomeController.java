package org.ozea.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import org.ozea.mapper.UserMapper;
import org.ozea.domain.User;

/**
 * 홈 및 로그인 페이지와 관련된 요청을 처리하는 컨트롤러입니다.
 */
@Controller
@Slf4j
public class HomeController {

    @Value("${kakao.api.key}")
    private String REST_API_KEY; // 카카오 REST API 키

    @Value("${kakao.redirect.uri}")
    private String REDIRECT_URI; // 카카오 로그인 후 리다이렉트될 URI

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
        // 폼에서 전달된 데이터 받기
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String mbti = request.getParameter("mbti");
        String phoneNum = request.getParameter("phoneNum");
        String birthDate = request.getParameter("birthDate");
        String sex = request.getParameter("sex");
        String salary = request.getParameter("salary");
        String payAmount = request.getParameter("payAmount");
        String role = request.getParameter("role");

        // User 객체 생성 및 값 세팅
        User user = new User();
        user.setUserId(java.util.UUID.randomUUID());
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password); // 이 단계에서는 비밀번호 암호화가 빠져 있음. 서비스에서 암호화 처리해야 안전함.
        user.setMbti(mbti);
        user.setPhoneNum(phoneNum);
        user.setBirthDate(java.time.LocalDate.parse(birthDate));
        user.setSex(sex);
        user.setSalary(Long.parseLong(salary));
        user.setPayAmount(Long.parseLong(payAmount));
        user.setRole(role);

        // DB에 저장
        userMapper.insertUser(user);

        // 회원가입 후 로그인 페이지로 이동
        return "redirect:/local-login";
    }

    /**
     * 로그인 페이지("/login") 요청을 처리합니다.
     * 모델에 카카오 API 키와 리다이렉트 URI를 추가하여 뷰에 전달합니다.
     * @param model 뷰에 데이터를 전달하기 위한 Model 객체
     * @return "login" - 로그인 페이지의 뷰 이름
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("REST_API_KEY", REST_API_KEY);
        model.addAttribute("REDIRECT_URI", REDIRECT_URI);
        return "login";
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

    @GetMapping("/additional-info")
    public String additionalInfoForm(Model model, HttpSession session) {
        model.addAttribute("email", session.getAttribute("email"));
        return "additional-info";
    }

    @PostMapping("/additional-info")
    public String saveAdditionalInfo(HttpServletRequest request) {
        String email = request.getParameter("email");
        String name = request.getParameter("name");
        String phoneNum = request.getParameter("phoneNum");
        String mbti = request.getParameter("mbti");
        String birthDate = request.getParameter("birthDate");
        String sex = request.getParameter("sex");
        String salary = request.getParameter("salary");
        String payAmount = request.getParameter("payAmount");
        String role = request.getParameter("role");

        org.ozea.domain.User user = userMapper.getUserByEmail(email);
        user.setName(name);
        user.setPhoneNum(phoneNum);
        user.setMbti(mbti);
        user.setBirthDate(java.time.LocalDate.parse(birthDate));
        user.setSex(sex);
        user.setSalary(Long.parseLong(salary));
        user.setPayAmount(Long.parseLong(payAmount));
        user.setRole(role);
        userMapper.updateUser(user);
        return "redirect:/main";
    }
}
