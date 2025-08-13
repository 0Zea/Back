package org.ozea.api.allaccount.controller;

import lombok.RequiredArgsConstructor;
import org.ozea.api.allaccount.service.AllAccountService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/allaccount")
public class AllAccountController {

    private final AllAccountService allAccountService;

    @PostMapping("/accounts")
    public ResponseEntity<String> getAccounts() throws Exception {
        String result = allAccountService.createAccountFromDB();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/yeomsky95/assets")
    public ResponseEntity<String> getYeomsky95Assets() throws Exception {
        String result = allAccountService.getYeomsky95Assets();
        return ResponseEntity.ok(result);
    }
}