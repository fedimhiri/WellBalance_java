package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import service.EmailService;
import service.UserService;

import java.util.Random;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    // Stockage temporaire statique partagé entre les 3 étapes
    public static String pendingEmail;
    public static String pendingCode;
    public static long   codeExpiry; // timestamp expiration (10 min)

    @FXML
    public void handleSendCode() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Veuillez saisir votre email.");
            return;
        }

        try {
            User user = userService.getUserByEmail(email);
            if (user == null) {
                showError("Aucun compte trouvé avec cet email.");
                return;
            }

            // Générer un code à 6 chiffres
            String code = String.format("%06d", new Random().nextInt(999999));

            // Stocker temporairement
            pendingEmail = email;
            pendingCode  = code;
            codeExpiry   = System.currentTimeMillis() + 10 * 60 * 1000; // +10 min

            // Envoyer l'email
            EmailService.sendVerificationCode(email, code);

            showSuccess("Code envoyé à " + email);

            // Naviguer vers la page de vérification
            navigateTo("/fxml/verify_code.fxml", "Vérification du code");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void goToLogin() {
        navigateTo("/fxml/login.fxml", "Connexion");
    }

    private void navigateTo(String path, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Scene scene = new Scene(loader.load(), 1500, 900);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: 600;");
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: 600;");
    }
}