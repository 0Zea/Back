package org.ozea.api.account.controller;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.ozea.api.account.dto.request.AllAccountReqDto;
import org.ozea.api.account.dto.response.AllAccountResDto;
import org.ozea.api.account.service.AllAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/AllAccount")
public class AllAccountController {
    private final AllAccountService allAccountService;
    @PostMapping("/accounts")
    public ResponseEntity<String> getAccounts(@RequestBody AllAccountReqDto reqDto) throws NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, ParseException, InvalidKeyException, InterruptedException {
        String result = allAccountService.createAccountFromDB();
        return ResponseEntity.ok(result);
    }
    @GetMapping("/yeomsky95/assets")
    public ResponseEntity<String> getYeomsky95Assets() throws NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, ParseException, InvalidKeyException, InterruptedException {
        String result = allAccountService.getYeomsky95Assets();
        return ResponseEntity.ok(result);
    }
}
