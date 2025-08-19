package org.ozea.api.util;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
public class RequestToken {
    public static String getToken(String clientId, String secretKey) throws IOException, InterruptedException, ParseException {
        try {
            URL url = new URL(CommonConstant.TOKEN_DOMAIN + CommonConstant.GET_TOKEN);
            String POST_PARAMS = "grant_type=client_credentials&scope=read";
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

            String auth = clientId + ":" + secretKey;
            String authStringEnc= Base64.getEncoder().encodeToString(auth.getBytes());
            String authHeader = "Basic " + authStringEnc;
            con.setRequestProperty("Authorization", authHeader);
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            os.write(POST_PARAMS.getBytes());
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(URLDecoder.decode(response.toString(), "UTF-8"));
            JSONObject tokenJson = (JSONObject)obj;

            return tokenJson.get("access_token").toString();
        } catch (Exception e) {
            System.err.println("Token request failed: " + e.getMessage());
        }
        return null;
    }
}
