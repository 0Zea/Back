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
@Controller
@Slf4j
public class HomeController {
    private final UserMapper userMapper;
    public HomeController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    @GetMapping("/")
    public String home() {
        return "index";
    }
    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup";
    }
    @PostMapping("/signup")
    public String processSignup(HttpServletRequest request) {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        User user = new User();
        user.setUserId(java.util.UUID.randomUUID());
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setMbti("미입력");
        user.setPhoneNum("000-0000-0000");
        user.setBirthDate(java.time.LocalDate.of(1900, 1, 1));
        user.setSex("female");
        user.setSalary(0L);
        user.setPayAmount(0L);
        user.setRole("USER");
        userMapper.insertUser(user);
        request.getSession().setAttribute("email", email);
        return "redirect:/additional-info";
    }
    @GetMapping("/local-login")
    public String showLocalLoginPage() {
        return "local-login";
    }
    @GetMapping("/main")
    public String mainPage() {
        return "main";
    }
}