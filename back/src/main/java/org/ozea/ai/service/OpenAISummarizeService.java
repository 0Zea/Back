package org.ozea.ai.service;

import org.ozea.ai.dto.ChatDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

public interface OpenAISummarizeService {
    String formatToThreeBullets (String content);
    ResponseEntity<String> postWithRetry(String url, HttpEntity<ChatDTO.ChatRequest> entity);
    String summarizeTo3Lines(String rawText);
    String limitLength(String s, int max);

}
