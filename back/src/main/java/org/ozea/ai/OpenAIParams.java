package org.ozea.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
public class OpenAIParams {
    @Value("${openai.baseUrl:https://api.openai.com}")
    private String baseUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.apiKey}")
    private String apiKey;

    @Value("${openai.timeoutMs:8000}")
    private int timeoutMs;
}