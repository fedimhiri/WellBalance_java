package utils;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Mini serveur HTTP local pour servir la page reCAPTCHA.
 * Google reCAPTCHA refuse les origines file:// et data: —
 * il faut servir le HTML via http://localhost pour que le widget fonctionne.
 */
public class RecaptchaHttpServer {

    private static HttpServer server;
    private static int port = 0; // 0 will auto-assign a free port
    private static boolean started = false;
    private static String siteKey = "6LcwVcUsAAAAAPqRJ7saH82fUlJxbKhJeoxoc4Uy";

    public static synchronized String start() {
        if (started) {
            return getUrl();
        }

        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
            port = server.getAddress().getPort(); // Get the actual assigned port
            server.createContext("/", new RecaptchaHandler());
            server.setExecutor(null);
            server.start();
            started = true;
            System.out.println("Serveur reCAPTCHA démarré sur " + getUrl());
            return getUrl();
        } catch (IOException e) {
            System.err.println("Erreur démarrage serveur reCAPTCHA: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static String getUrl() {
        return "http://127.0.0.1:" + port + "/";
    }

    /**
     * Arrête le serveur HTTP local.
     */
    public static synchronized void stop() {
        if (server != null && started) {
            server.stop(0);
            started = false;
        }
    }

    private static class RecaptchaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html>\n"
                    + "<html lang='fr'>\n"
                    + "<head>\n"
                    + "  <meta charset='UTF-8'>\n"
                    + "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n"
                    + "  <script src='https://www.google.com/recaptcha/api.js' async defer></script>\n"
                    + "  <style>\n"
                    + "    body { margin:0; padding:10px; background-color: #fff9e6; display: flex; align-items: center; justify-content: center; height: 100vh; flex-direction: column; }\n"
                    + "  </style>\n"
                    + "  <script>\n"
                    + "    function onSuccess(token) {\n"
                    + "      if(window.javaConnector) { window.javaConnector.setToken(token); }\n"
                    + "    }\n"
                    + "    function onExpired() {\n"
                    + "      if(window.javaConnector) { window.javaConnector.resetToken(); }\n"
                    + "    }\n"
                    + "  </script>\n"
                    + "</head>\n"
                    + "<body>\n"
                    + "  <div id='loading' style='color: #888; margin-bottom: 5px; font-family: sans-serif; font-size: 12px;'>Chargement du captcha...</div>\n"
                    + "  <div class='g-recaptcha'\n"
                    + "       data-sitekey='" + siteKey + "'\n"
                    + "       data-callback='onSuccess'\n"
                    + "       data-expired-callback='onExpired'>\n"
                    + "  </div>\n"
                    + "</body>\n"
                    + "</html>";

            byte[] content = html.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        }
    }
}
