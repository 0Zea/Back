package org.ozea.api.taxkakaoouth.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ozea.api.taxkakaoouth.service.TaxKakaoOuthService;
import org.ozea.taxinfo.dto.TaxInfoItemDto;
import org.ozea.taxinfo.dto.TaxInfoReqDto;
import org.ozea.taxinfo.service.TaxInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController // @Controller + @ResponseBody
@RequestMapping("/api/tax")
@RequiredArgsConstructor
public class TaxKakaoOuthController {
    private final TaxInfoService taxInfoService;          // ← 추가
    private final TaxKakaoOuthService taxKakaoOuthService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/auth")
    public ResponseEntity<String> processKakaoAuth(
            @RequestParam String userId,
            @RequestParam(required = false) String year
    ) {
        try {
            UUID uuid = UUID.fromString(userId);
            // 서비스에서 이미 URL 디코딩 끝난 JSON 문자열이 옴
            String jsonResponse = taxKakaoOuthService.processKakaoAuth(uuid, year);
            // 프론트로 그대로 전달

            JsonNode root = objectMapper.readTree(jsonResponse);
            String resultCode = root.path("result").path("code").asText();

            if ("CF-00000".equals(resultCode)) {
                // data 배열 → DTO
                List<TaxInfoItemDto> items = objectMapper.convertValue(
                        root.path("data"),
                        new TypeReference<List<TaxInfoItemDto>>() {}
                );
                // userId 채우기
                TaxInfoReqDto req = new TaxInfoReqDto();
                req.setUserId(userId);
                // 저장
                taxInfoService.saveAndSummary(req);
            }

            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    "{\"result\":{\"code\":\"SERVER_ERROR\",\"message\":\"" + e.getMessage() + "\"}}"
            );
        }
    }
}
