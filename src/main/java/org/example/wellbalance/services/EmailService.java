package org.example.wellbalance.services;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.example.wellbalance.models.RendezVous;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    private static final String CONFIG_FILE = "/mail.properties";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Properties config = new Properties();

    public EmailService() {
        loadConfiguration();
    }

    public EmailResult envoyerNotificationStatut(String destinataire, RendezVous rendezVous, String statut) {
        if (!isEnabled()) {
            return EmailResult.skipped("Envoi email desactive dans mail.properties.");
        }
        destinataire = resolveRecipient(destinataire);
        if (destinataire == null || destinataire.isBlank()) {
            return EmailResult.skipped("Aucune adresse email utilisateur trouvee pour ce rendez-vous.");
        }

        String username = getConfig("mail.username");
        String password = getConfig("mail.password");
        String from = getConfig("mail.from");
        if (from.isBlank()) {
            from = username;
        }

        if (username.isBlank() || password.isBlank() || from.isBlank()) {
            return EmailResult.skipped("Configuration SMTP incomplete: mail.username, mail.password et mail.from sont requis.");
        }

        try {
            Message message = new MimeMessage(createSession(username, password));
            message.setFrom(createAddress(from, getConfig("mail.from.name")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("Mise a jour de votre rendez-vous");
            message.setText(buildBody(rendezVous, statut));
            Transport.send(message);
            return EmailResult.sentResult();
        } catch (MessagingException | UnsupportedEncodingException e) {
            return EmailResult.failed("Erreur envoi email: " + e.getMessage());
        }
    }

    private Session createSession(String username, String password) {
        Properties props = new Properties();
        props.put("mail.smtp.host", getConfig("mail.smtp.host"));
        props.put("mail.smtp.port", getConfig("mail.smtp.port"));
        props.put("mail.smtp.auth", getConfig("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", getConfig("mail.smtp.starttls.enable"));

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private InternetAddress createAddress(String email, String name) throws MessagingException, UnsupportedEncodingException {
        return name == null || name.isBlank()
                ? new InternetAddress(email)
                : new InternetAddress(email, name);
    }

    private String buildBody(RendezVous rendezVous, String statut) {
        String type = rendezVous.getTypeRendezVous() == null ? "Non precise" : rendezVous.getTypeRendezVous().getLibelle();
        String date = rendezVous.getDateRdv() == null ? "Non precisee" : rendezVous.getDateRdv().format(DATE_FORMATTER);
        String heure = rendezVous.getHeureRdv() == null ? "Non precisee" : rendezVous.getHeureRdv().format(TIME_FORMATTER);

        return """
                Bonjour,

                Votre rendez-vous a ete traite par l'administration.

                Statut: %s
                Date: %s
                Heure: %s
                Type: %s

                Merci,
                L'equipe WellBalance
                """.formatted(statut, date, heure, type);
    }

    private void loadConfiguration() {
        try (InputStream input = EmailService.class.getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                config.load(input);
            }
        } catch (IOException e) {
            System.out.println("Configuration email non chargee: " + e.getMessage());
        }
    }

    private boolean isEnabled() {
        return Boolean.parseBoolean(getConfig("mail.enabled", "true"));
    }

    private String resolveRecipient(String destinataire) {
        if (destinataire != null && !destinataire.isBlank()) {
            return destinataire.trim();
        }
        // Utilise une adresse de test tant que l'email user n'est pas encore rattache au rendez-vous.
        return getConfig("mail.default.recipient");
    }

    private String getConfig(String key) {
        return getConfig(key, "");
    }

    private String getConfig(String key, String defaultValue) {
        String envValue = System.getenv(toEnvName(key));
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        return config.getProperty(key, defaultValue).trim();
    }

    private String toEnvName(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    public record EmailResult(boolean sent, boolean skipped, String message) {
        public static EmailResult sentResult() {
            return new EmailResult(true, false, "Email envoye.");
        }

        public static EmailResult skipped(String message) {
            return new EmailResult(false, true, message);
        }

        public static EmailResult failed(String message) {
            return new EmailResult(false, false, message);
        }
    }
}
