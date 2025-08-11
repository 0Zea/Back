package org.ozea.ai.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.ozea.ai.OpenAIParams;
import org.ozea.ai.dto.ChatDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Log4j2
@Service
public class OpenAISummarizeService {

    private final RestTemplate openAiRestTemplate;
    private final OpenAIParams params;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public OpenAISummarizeService(
            @org.springframework.beans.factory.annotation.Qualifier("openAiRestTemplate")
            RestTemplate openAiRestTemplate,
            OpenAIParams params) {
        this.openAiRestTemplate = openAiRestTemplate;
        this.params = params;
    }

    public String summarizeTo3Lines(String rawText) {
        String base = params.getBaseUrl();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        String url = (base.endsWith("/v1")) ? base + "/chat/completions" : base + "/v1/chat/completions";
        log.debug("Calling OpenAI URL: {}", url);

        ChatDTO.ChatRequest req = ChatDTO.ChatRequest.builder()
                .model(params.getModel())
                .temperature(0.2)
                .messages(List.of(
                        ChatDTO.ChatRequest.Message.builder()
                                .role("system")
                                .content("너는 금융상품 설명을 이해해서 핵심만 한국어로 3줄 bullet로 요약하는 보조자야. 각 줄은 1문장, 불필요한 수식어 금지.")
                                .build(),
                        ChatDTO.ChatRequest.Message.builder()
                                .role("user")
                                .content("아래 내용을 3줄로 요약해줘:\n\n" + rawText)
                                .build()
                ))
                .build();


        try {
            HttpEntity<ChatDTO.ChatRequest> entity = new HttpEntity<>(req);
            ResponseEntity<String> res =
                    openAiRestTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (!res.getStatusCode().is2xxSuccessful()) {
                log.error("OpenAI non-2xx: status={}, body={}", res.getStatusCodeValue(), res.getBody());
                throw new IllegalStateException("OpenAI returned non-2xx");
            }

            ChatDTO.ChatResponse body =
                    objectMapper.readValue(res.getBody(), ChatDTO.ChatResponse.class);

            if (body.getChoices() == null || body.getChoices().isEmpty()) {
                throw new IllegalStateException("OpenAI 응답에 choices가 없습니다.");
            }
            String content = body.getChoices().get(0).getMessage().getContent();
            if (content == null) {
                throw new IllegalStateException("OpenAI 응답 content가 없습니다.");
            }
            return formatToThreeBullets(content.trim());

        } catch (HttpStatusCodeException httpEx) {
            log.error("OpenAI HTTP error: status={}, body={}",
                    httpEx.getStatusCode().value(), httpEx.getResponseBodyAsString(), httpEx);
            throw new RuntimeException("외부 요약 호출 실패 (http)", httpEx);
        } catch (Exception e) {
            log.error("OpenAI 요약 실패: {}", e.getMessage(), e);
            throw new RuntimeException("외부 요약 호출 실패", e);
        }
    }
    private String formatToThreeBullets (String content){
        if (content == null) return null;
        String[] rawLines = content.replace("\r", "").split("\n");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String line : rawLines) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            t = t.replaceFirst("^[•\\-\\*\\d\\.)\\(]+\\s*", ""); // 앞 bullet 제거
            if (sb.length() > 0) sb.append("\n");
            sb.append("• ").append(t);
            if (++count == 3) break;
        }
        if (count == 0 && content.length() > 0) { // 문단만 온 경우
            String[] parts = content.split("(?<=\\.)\\s+");
            for (int i = 0; i < parts.length && i < 3; i++) {
                String t = parts[i].trim();
                if (t.isEmpty()) continue;
                if (!t.endsWith(".")) t += ".";
                if (sb.length() > 0) sb.append("\n");
                sb.append("• ").append(t);
            }
        }
        return sb.toString().trim();
    }
}