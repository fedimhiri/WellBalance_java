package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import model.User;
import org.mindrot.jbcrypt.BCrypt;
import service.UserService;

public class ResetPasswordController {

    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    public void handleReset() {
        String password = passwordField.getText();
        String confirm  = confirmPasswordField.getText();

        if (password.isEmpty() || confirm.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            User user = userService.getUserByEmail(ForgotPasswordController.pendingEmail);

            // ✅ Hachage BCrypt
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            user.setPassword(hashed);
            userService.modifier(user);

            // Nettoyer les données temporaires
            ForgotPasswordController.pendingEmail = null;
            ForgotPasswordController.pendingCode  = null;
            ForgotPasswordController.codeExpiry   = 0;

            showSuccess("Mot de passe réinitialisé avec succès !");

            // Retour login après 1.5s
            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(e -> navigateTo("/fxml/login.fxml", "Connexion"));
            pause.play();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur : " + e.getMessage());
        }
    }

    private void navigateTo(String path, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Scene scene = new Scene(loader.load(), 1500, 900);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );
            Stage stage = (Stage) passwordField.getScene().getWindow();
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