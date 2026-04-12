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
        message.setText(
                "Bonjour,\n\n" +
                        "Votre code de vérification est : " + code + "\n\n" +
                        "Ce code expire dans 10 minutes.\n\n" +
                        "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email."
        );

        Transport.send(message);
    }
}