package org.ozea.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.user.dto.UserDTO;
import org.ozea.user.dto.UserSignupDTO;
import org.ozea.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
    final UserService service;

    // 이메일 중복 확인
    @GetMapping("/signup/check/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.checkEmail(email));
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody UserSignupDTO user) {
        return ResponseEntity.ok(service.signup(user));
    }

    // 이메일을 기준으로 사용자 정보를 조회
    @GetMapping("/user/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.getUserByEmail(email));
    }
}
