package org.ozea.api.util;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

public class HttpRequest {
    public static Object post(String url_path, String token, String bodyString) {
        try {
            URL url = new URL(url_path);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            con.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            if (token != null) {
                con.setRequestProperty("Authorization", "Bearer " + token);
            }

            OutputStream os = con.getOutputStream();
            os.write(bodyString.getBytes());
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

            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
