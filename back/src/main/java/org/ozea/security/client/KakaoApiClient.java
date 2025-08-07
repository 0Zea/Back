package org.ozea.security.client;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
@Log4j2
@Component
public class KakaoApiClient {
    private static final String KAKAO_API_BASE_URL = "https://kapi.kakao.com";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    public boolean unlink(String accessToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(KAKAO_API_BASE_URL + "/v1/user/unlink"))
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}