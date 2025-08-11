package org.ozea.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.ai.dto.SummaryReq;
import org.ozea.ai.service.OpenAISummarizeService;
import org.ozea.common.dto.ApiResponse;
import org.ozea.common.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final OpenAISummarizeService summarizeService;

    @PostMapping("/summarize")
    public ResponseEntity<ApiResponse<String>> summarize(@RequestBody SummaryReq req) {
        if (req == null || req.getText() == null || req.getText().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.INVALID_INPUT, "요약할 텍스트가 비어있습니다."));
        }
        try {
            String summary = summarizeService.summarizeTo3Lines(req.getText());
            return ResponseEntity.ok(ApiResponse.success(summary, "요약 성공"));
        } catch (Exception e) {
            log.error("요약 실패: {}", e.getMessage());
            return ResponseEntity.status(502) // Bad Gateway 성격
                    .body(ApiResponse.error(ErrorCode.EXTERNAL_API_ERROR, "OpenAI 요약 실패"));
        }
    }
}