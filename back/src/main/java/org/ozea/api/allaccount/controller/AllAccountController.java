package org.ozea.api.allaccount.controller;

import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.ozea.api.allaccount.service.AllAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/allaccount")
public class AllAccountController {
    private final AllAccountService allAccountService;
    
    @GetMapping
    public ResponseEntity<String> getAllAccount(@RequestParam String userId) throws NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, ParseException, InvalidKeyException, InterruptedException {
        String result = allAccountService.getAllAccount(userId);
        return ResponseEntity.ok(result);
    }
}