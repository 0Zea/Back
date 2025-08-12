package org.ozea.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.ai.dto.SummaryReq;
import org.ozea.ai.service.OpenAISummarizeServiceImpl;
import org.ozea.common.dto.ApiResponse;
import org.ozea.common.exception.ErrorCode;
import org.ozea.common.limiter.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final OpenAISummarizeServiceImpl summarizeService;
    private final RateLimiter rateLimiter;

    @PostMapping("/summarize")
    public ResponseEntity<ApiResponse<String>> summarize(@RequestBody SummaryReq req,
                                                         @org.springframework.security.core.annotation.AuthenticationPrincipal Object me) {
        String uid = (me != null) ? me.toString() : "anon";
        String bucket = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String key = "rl:ai:" + uid + ":" + bucket;
        if (!rateLimiter.allow(key, java.time.Duration.ofMinutes(1), 20)) {
            return ResponseEntity.status(429).body(ApiResponse.error(ErrorCode.TOO_MANY_REQUESTS, "요청이 너무 많습니다."));
        }
        if (req == null || req.getText() == null || req.getText().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INVALID_INPUT, "요약할 텍스트가 비어있습니다."));
        }
        try {
            String summary = summarizeService.summarizeTo3Lines(req.getText());
            return ResponseEntity.ok(ApiResponse.success(summary, "요약 성공"));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(ApiResponse.error(ErrorCode.EXTERNAL_API_ERROR, "OpenAI 요약 실패"));
        }
    }
}