package controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.User;
import org.mindrot.jbcrypt.BCrypt;
import service.UserService;

public class ResetPasswordController {

    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label passwordStrengthLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
            clearMessage();
        });

        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> clearMessage());

        updatePasswordStrength("");
    }

    @FXML
    public void handleReset() {
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        String confirm  = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText().trim();

        if (password.isEmpty() || confirm.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (calculatePasswordStrength(password) < 0.5) {
            showError("Le mot de passe est trop faible.");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        if (ForgotPasswordController.pendingEmail == null || ForgotPasswordController.pendingEmail.isBlank()) {
            showError("Session expirée. Veuillez recommencer la procédure.");
            return;
        }

        try {
            User user = userService.getUserByEmail(ForgotPasswordController.pendingEmail);

            if (user == null) {
                showError("Utilisateur introuvable.");
                return;
            }

            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            user.setPassword(hashed);
            userService.modifier(user);

            ForgotPasswordController.pendingEmail = null;
            ForgotPasswordController.pendingCode = null;
            ForgotPasswordController.codeExpiry = 0;

            showSuccess("Mot de passe réinitialisé avec succès !");

            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> navigateTo("/fxml/login.fxml", "Connexion"));
            pause.play();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur : " + e.getMessage());
        }
    }

    private void updatePasswordStrength(String password) {
        if (passwordStrengthBar == null || passwordStrengthLabel == null) {
            return;
        }

        double strength = calculatePasswordStrength(password);
        passwordStrengthBar.setProgress(strength);

        passwordStrengthBar.getStyleClass().removeAll(
                "strength-weak",
                "strength-medium",
                "strength-strong",
                "strength-empty"
        );

        if (password == null || password.isEmpty()) {
            passwordStrengthBar.getStyleClass().add("strength-empty");
            passwordStrengthLabel.setText("Vide");
            passwordStrengthLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else if (strength < 0.4) {
            passwordStrengthBar.getStyleClass().add("strength-weak");
            passwordStrengthLabel.setText("Faible");
            passwordStrengthLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else if (strength < 0.75) {
            passwordStrengthBar.getStyleClass().add("strength-medium");
            passwordStrengthLabel.setText("Moyen");
            passwordStrengthLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            passwordStrengthBar.getStyleClass().add("strength-strong");
            passwordStrengthLabel.setText("Fort");
            passwordStrengthLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
    }

    private double calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0.0;
        }

        int score = 0;

        if (password.length() >= 6) score++;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[^a-zA-Z0-9].*")) score++;

        return score / 6.0;
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

    private void clearMessage() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: 600; -fx-font-size: 13px;");
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: 600; -fx-font-size: 13px;");
    }
}