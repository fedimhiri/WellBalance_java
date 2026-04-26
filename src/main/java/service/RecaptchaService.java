package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RecaptchaService {

    private static final String SECRET_KEY = "6LcwVcUsAAAAAI26ahI6lp2pegUVMGQef_-cSyfb";

    public boolean verifyToken(String token) {
        try {
            String url = "https://www.google.com/recaptcha/api/siteverify";

            String params = "secret=" + SECRET_KEY +
                    "&response=" + token;

            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = con.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            String result = response.toString();
            System.out.println("Réponse Google reCAPTCHA : " + result);

            return result.contains("\"success\": true") || result.contains("\"success\":true");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}