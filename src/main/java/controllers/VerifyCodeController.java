package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VerifyCodeController {

    @FXML private TextField codeField;
    @FXML private Label messageLabel;

    @FXML
    public void handleVerify() {
        String code = codeField.getText().trim();

        if (code.isEmpty()) {
            showError("Veuillez saisir le code.");
            return;
        }

        // Vérifier expiration
        if (System.currentTimeMillis() > ForgotPasswordController.codeExpiry) {
            showError("Le code a expiré. Veuillez recommencer.");
            return;
        }

        // Vérifier le code
        if (!code.equals(ForgotPasswordController.pendingCode)) {
            showError("Code incorrect.");
            return;
        }

        // Code valide → page reset password
        navigateTo("/fxml/reset_password.fxml", "Réinitialiser le mot de passe");
    }

    @FXML
    public void goBack() {
        navigateTo("/fxml/forgot_password.fxml", "Mot de passe oublié");
    }

    private void navigateTo(String path, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Scene scene = new Scene(loader.load(), 1500, 900);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );
            Stage stage = (Stage) codeField.getScene().getWindow();
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
}