package org.ozea.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class CodefApiRequest {

    private final ObjectMapper objectMapper;

    public String request(String urlPath, HashMap<String, Object> bodyMap)
            throws IOException, InterruptedException, ParseException {

        String accessToken = CommonConstant.ACCESS_TOKEN;
        if (!StringUtils.hasText(accessToken)) {
            accessToken = RequestToken.getToken(CommonConstant.CLIENT_ID, CommonConstant.SECERET_KEY);
            CommonConstant.ACCESS_TOKEN = accessToken;
        }

        String bodyString = objectMapper.writeValueAsString(bodyMap);

        JSONObject json = (JSONObject) HttpRequest.post(urlPath, accessToken, bodyString);

        Object error = json.get("error");
        if ("invalid_token".equals(error)) {
            accessToken = RequestToken.getToken(CommonConstant.CLIENT_ID, CommonConstant.SECERET_KEY);
            CommonConstant.ACCESS_TOKEN = accessToken;

            json = (JSONObject) HttpRequest.post(urlPath, accessToken, bodyString);
        }

        String raw = json.toJSONString();
        Object pretty = objectMapper.readValue(raw, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pretty);
    }
}