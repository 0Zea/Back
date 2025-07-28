package org.ozea.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
        org.ozea.user.domain.User user = userMapper.getUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다: " + email);
        }
        String phoneNum = request.getParameter("phoneNum");
        String birthDate = request.getParameter("birthDate");
        String sex = request.getParameter("sex");
        String salary = request.getParameter("salary");
        String payAmount = request.getParameter("payAmount");
        user.setPhoneNum(phoneNum);
        user.setBirthDate(java.time.LocalDate.parse(birthDate));
        user.setSex(sex);
        user.setSalary(Long.parseLong(salary));
        user.setPayAmount(Long.parseLong(payAmount));
        user.setRole("USER");
        userMapper.updateUser(user);
        return "redirect:/mbti-survey";
    }

    @GetMapping("/mbti-survey")
    public String mbtiSurveyForm(Model model, HttpSession session) {
        model.addAttribute("email", session.getAttribute("email"));
        return "mbti-survey";
    }

    @PostMapping("/mbti-survey")
    public String saveMbtiSurvey(HttpServletRequest request) {
        String email = request.getParameter("email");
        // 빠름/느림 점수 계산
        int fast = 0, slow = 0, high = 0, low = 0;
        if ("fast".equals(request.getParameter("speed1"))) fast += 4; else slow += 4;
        if ("fast".equals(request.getParameter("speed2"))) fast += 4; else slow += 4;
        if ("fast".equals(request.getParameter("speed3"))) fast += 4; else slow += 4;
        if ("high".equals(request.getParameter("risk1"))) high += 4; else low += 4;
        if ("high".equals(request.getParameter("risk2"))) high += 4; else low += 4;
        if ("high".equals(request.getParameter("risk3"))) high += 4; else low += 4;
        // 유형 판별
        String mbtiType;
        if (fast > slow && high > low) mbtiType = "신속한 승부사";
        else if (slow >= fast && high > low) mbtiType = "신중한 승부사";
        else if (fast > slow && low >= high) mbtiType = "신속한 분석가";
        else mbtiType = "신중한 분석가";
        // DB 저장
        org.ozea.user.domain.User user = userMapper.getUserByEmail(email);
        user.setMbti(mbtiType);
        userMapper.updateUser(user);
        return "redirect:/";
    }
} 