package org.ozea.ai.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.ozea.ai.OpenAIParams;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenAIConfig {

    @Bean(name = "openAiRestTemplate")
    public RestTemplate openAiRestTemplate(OpenAIParams props) {
        RequestConfig rc = RequestConfig.custom()
                .setConnectTimeout(props.getTimeoutMs())
                .setConnectionRequestTimeout(props.getTimeoutMs())
                .setSocketTimeout(props.getTimeoutMs())
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(rc)
                .disableAutomaticRetries()
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate rt = new RestTemplate(factory);

        rt.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("Authorization", "Bearer " + props.getApiKey());
            request.getHeaders().set("Content-Type", "application/json; charset=utf-8");
            String url = request.getURI().toString();
            org.apache.logging.log4j.LogManager.getLogger("OpenAI")
                    .debug("→ OpenAI call: {} bodyBytes={}", url, body != null ? body.length : 0);
            ClientHttpResponse resp = execution.execute(request, body);
            org.apache.logging.log4j.LogManager.getLogger("OpenAI")
                    .debug("← OpenAI status: {}", resp.getStatusCode().value());
            return resp;
        });
        return rt;
    }
}