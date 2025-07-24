package org.ozea.util;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.util.StringUtils;


//AccessToken 발급 받는 곳
public class CodefApiRequest {
    private static ObjectMapper mapper = new ObjectMapper();

    public static String request(String urlPath, HashMap<String, Object>bodyMap) throws IOException ,InterruptedException, ParseException{
        String accessToken = CommonConstant.ACCESS_TOKEN;
        if(StringUtils.isEmpty(accessToken)){
            accessToken = CommonConstant.ACCESS_TOKEN;
            CommonConstant.ACCESS_TOKEN = accessToken;
        }

        //POST요청을 위한 리퀘스트바디 생성(UTF-8 인코딩)
        String bodyString = mapper.writeValueAsString(bodyMap);
        bodyString = URLEncoder.encode(bodyString, "UTF-8");

        //API 요청
        JSONObject json = (JSONObject) HttpRequest.post(urlPath, accessToken, bodyString);
        String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        // 액세스 토큰 유효기간 만료시
        if("invalid_token".equals(json.get("error"))) {
            // 토큰 재발급 요청 수행
            accessToken = RequestToken.getToken(CommonConstant.CLIENT_ID, CommonConstant.SECERET_KEY);
            CommonConstant.ACCESS_TOKEN = accessToken;	// 재사용을 위한 발급받은 액세스 토큰 저장

            // API 재요청
            json = (JSONObject) HttpRequest.post(urlPath, accessToken, bodyString);
            result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

        } else if("access_denied".equals(json.get("error"))) {
            System.out.println("access_denied은 API 접근 권한이 없는 경우입니다.");
            System.out.println("코드에프 대시보드의 API 설정을 통해 해당 업무 접근 권한을 설정해야 합니다.");
        }

        return result;
    }

}
