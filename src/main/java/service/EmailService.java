package service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String FROM     = "mhirifedi22@gmail.com"; // ← votre Gmail
    private static final String PASSWORD = "fhsh bupk tvob twly";   // ← App Password Gmail (16 chars)

    public static void sendVerificationCode(String toEmail, String code) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Code de vérification - Clinique Médicale");
        
        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }" +
                "        .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); }" +
                "        .header { background: linear-gradient(135deg, #007bff 0%, #0056b3 100%); padding: 40px 20px; text-align: center; color: white; }" +
                "        .header h1 { margin: 0; font-size: 28px; font-weight: 600; }" +
                "        .content { padding: 40px 30px; color: #333333; line-height: 1.6; text-align: center; }" +
                "        .badge { background-color: #e7f1ff; color: #007bff; padding: 5px 15px; border-radius: 20px; font-size: 12px; font-weight: 600; display: inline-block; margin-bottom: 20px; }" +
                "        .code-container { background-color: #f8f9fa; border: 2px dashed #007bff; border-radius: 8px; padding: 20px; margin: 20px 0; display: inline-block; }" +
                "        .verification-code { font-size: 42px; font-weight: 700; color: #007bff; letter-spacing: 10px; margin: 0; }" +
                "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; font-size: 14px; color: #6c757d; border-top: 1px solid #eeeeee; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'><h1>Clinique Médicale</h1></div>" +
                "        <div class='content'>" +
                "            <div class='badge'>VÉRIFICATION DE COMPTE</div>" +
                "            <p>Bonjour,</p>" +
                "            <p>Nous avons reçu une demande de code de vérification pour votre compte. Utilisez le code ci-dessous pour continuer :</p>" +
                "            <div class='code-container'><h2 class='verification-code'>" + code + "</h2></div>" +
                "            <p style='color: #666; font-size: 14px; margin-top: 30px;'>" +
                "                Ce code expirera dans <strong>10 minutes</strong>.<br>" +
                "                Si vous n'avez pas demandé ce code, vous pouvez ignorer cet email en toute sécurité." +
                "            </p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>&copy; 2026 Clinique Médicale. Tous droits réservés.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";

        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);
    }
}