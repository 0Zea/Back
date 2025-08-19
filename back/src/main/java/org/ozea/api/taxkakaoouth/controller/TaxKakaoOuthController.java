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

@RestController
@RequestMapping("/api/tax")
@RequiredArgsConstructor
public class TaxKakaoOuthController {
    private final TaxInfoService taxInfoService;
    private final TaxKakaoOuthService taxKakaoOuthService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/auth")
    public ResponseEntity<String> processKakaoAuth(
            @RequestParam String userId,
            @RequestParam(required = false) String year
    ) {
        try {
            UUID uuid = UUID.fromString(userId);
            String jsonResponse = taxKakaoOuthService.processKakaoAuth(uuid, year);

            JsonNode root = objectMapper.readTree(jsonResponse);
            String resultCode = root.path("result").path("code").asText();

            if ("CF-00000".equals(resultCode)) {
                List<TaxInfoItemDto> items = objectMapper.convertValue(
                        root.path("data"),
                        new TypeReference<List<TaxInfoItemDto>>() {}
                );
                TaxInfoReqDto req = new TaxInfoReqDto();
                req.setUserId(userId);
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
