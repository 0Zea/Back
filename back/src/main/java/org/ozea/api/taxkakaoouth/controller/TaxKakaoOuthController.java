package org.ozea.api.taxkakaoouth.controller;

import lombok.RequiredArgsConstructor;
import org.ozea.api.taxkakaoouth.service.TaxKakaoOuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController // @Controller + @ResponseBody
@RequestMapping("/tax")
@RequiredArgsConstructor
public class TaxKakaoOuthController {

    private final TaxKakaoOuthService taxKakaoOuthService;

    /**
     * Step1 + Step2를 한 번에 처리
     */
    @GetMapping("/auth")
    public ResponseEntity<?> processKakaoAuth(@RequestParam UUID userId) {
        try {
            System.out.println("받은 userId: " + userId);
            var response = taxKakaoOuthService.processKakaoAuth(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
