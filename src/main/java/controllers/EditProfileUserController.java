package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import service.UserService;
import utils.Session;

import java.io.IOException;
import java.sql.SQLException;

public class EditProfileUserController {

    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    // Labels d'erreur inline
    @FXML private Label emailError;
    @FXML private Label usernameError;
    @FXML private Label telephoneError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;

    private User user;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        user = Session.getCurrentUser();
        if (user != null) {
            emailField.setText(user.getEmail());
            usernameField.setText(user.getUsername());
            telephoneField.setText(user.getTelephone());
        }

        // Effacer erreur dès que l'utilisateur retape
        emailField.textProperty().addListener((o, oldVal, newVal)           -> clearError(emailField, emailError));
        usernameField.textProperty().addListener((o, oldVal, newVal)        -> clearError(usernameField, usernameError));
        telephoneField.textProperty().addListener((o, oldVal, newVal)       -> clearError(telephoneField, telephoneError));
        passwordField.textProperty().addListener((o, oldVal, newVal)        -> clearError(passwordField, passwordError));
        confirmPasswordField.textProperty().addListener((o, oldVal, newVal) -> clearError(confirmPasswordField, confirmPasswordError));
    }

    @FXML
    public void handleSave() {
        String email     = emailField.getText().trim();
        String username  = usernameField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String password  = passwordField.getText();
        String confirm   = confirmPasswordField.getText();

        if (user == null) {
            showError("Aucun utilisateur chargé.");
            return;
        }

        boolean valid = true;

        // Email
        if (email.isEmpty()) {
            showFieldError(emailField, emailError, "L'email est obligatoire.");
            valid = false;
        } else if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showFieldError(emailField, emailError, "Format d'email invalide.");
            valid = false;
        }

        // Username
        if (username.isEmpty()) {
            showFieldError(usernameField, usernameError, "Le nom d'utilisateur est obligatoire.");
            valid = false;
        } else if (username.length() < 3) {
            showFieldError(usernameField, usernameError, "Minimum 3 caractères.");
            valid = false;
        }

        // Téléphone
        if (telephone.isEmpty()) {
            showFieldError(telephoneField, telephoneError, "Le téléphone est obligatoire.");
            valid = false;
        } else if (!telephone.matches("\\d{8}")) {
            showFieldError(telephoneField, telephoneError, "Exactement 8 chiffres requis.");
            valid = false;
        }

        // Mot de passe (optionnel mais validé si rempli)
        if (!password.isEmpty()) {
            if (password.length() < 6) {
                showFieldError(passwordField, passwordError, "Minimum 6 caractères.");
                valid = false;
            } else if (!password.equals(confirm)) {
                showFieldError(confirmPasswordField, confirmPasswordError, "Les mots de passe ne correspondent pas.");
                valid = false;
            }
        }

        if (!valid) return;

        try {
            user.setEmail(email);
            user.setUsername(username);
            user.setTelephone(telephone);

            if (!password.isEmpty()) {
                user.setPassword(password);
            }

            userService.modifier(user);
            Session.setCurrentUser(user);

            showSuccess("Profil mis à jour avec succès !");

        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @FXML
    public void goToFrontOffice() {
        navigateTo("/fxml/front.fxml", "Front Office");
    }

    @FXML
    public void logout() {
        Session.clear();
        navigateTo("/fxml/login.fxml", "Connexion");
    }

    // ===== Helpers =====

    private void showFieldError(Control field, Label errorLabel, String message) {
        if (field != null) {
            field.setStyle(
                    "-fx-border-color: #e74c3c; -fx-border-width: 1.5; " +
                            "-fx-border-radius: 8; -fx-background-radius: 8;"
            );
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void clearError(Control field, Label errorLabel) {
        field.setStyle("");
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1400, 850);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible de charger la page : " + e.getMessage());
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