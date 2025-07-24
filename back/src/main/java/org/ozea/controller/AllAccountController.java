package org.ozea.controller;

import org.json.simple.parser.ParseException;
import org.ozea.dto.response.AllAccountResDto;
import org.ozea.service.apisetting.AllAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/AllAccount")
public class AllAccountController {
    private final AllAccountService allAccountService;

    @Autowired
    public AllAccountController(AllAccountService allAccountService) {
        this.allAccountService = allAccountService;
    }

    @PostMapping("/accounts")
    @ResponseBody // 이 메서드만 JSON 반환
    public ResponseEntity<String> getAccounts(@RequestBody AllAccountResDto request)
            throws IOException, ParseException, InterruptedException {
        String result = allAccountService.getAccountData(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/testAllAccount")
    public String testAllAccountView() {
        return "AllAccountTest"; // JSP 반환
    }
}