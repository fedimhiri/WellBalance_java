package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import service.UserService;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox agreeTermsCheckBox;

    // Labels d'erreur inline — à ajouter dans le FXML
    @FXML private Label emailError;
    @FXML private Label usernameError;
    @FXML private Label telephoneError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label termsError;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Effacer l'erreur dès que l'utilisateur retape
        emailField.textProperty().addListener((o, oldVal, newVal) -> clearError(emailField, emailError));
        usernameField.textProperty().addListener((o, oldVal, newVal) -> clearError(usernameField, usernameError));
        telephoneField.textProperty().addListener((o, oldVal, newVal) -> clearError(telephoneField, telephoneError));
        passwordField.textProperty().addListener((o, oldVal, newVal) -> clearError(passwordField, passwordError));
        confirmPasswordField.textProperty().addListener((o, oldVal, newVal) -> clearError(confirmPasswordField, confirmPasswordError));
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        String email           = emailField.getText().trim();
        String username        = usernameField.getText().trim();
        String telephone       = telephoneField.getText().trim();
        String password        = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

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

        // Mot de passe
        if (password.isEmpty()) {
            showFieldError(passwordField, passwordError, "Le mot de passe est obligatoire.");
            valid = false;
        } else if (password.length() < 6) {
            showFieldError(passwordField, passwordError, "Minimum 6 caractères.");
            valid = false;
        }

        // Confirmation mot de passe
        if (confirmPassword.isEmpty()) {
            showFieldError(confirmPasswordField, confirmPasswordError, "Veuillez confirmer le mot de passe.");
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            showFieldError(confirmPasswordField, confirmPasswordError, "Les mots de passe ne correspondent pas.");
            valid = false;
        }

        // Conditions
        if (!agreeTermsCheckBox.isSelected()) {
            showFieldError(null, termsError, "Vous devez accepter les conditions.");
            valid = false;
        }

        if (!valid) return;

        try {
            User existingUser = userService.getUserByEmail(email);
            if (existingUser != null) {
                showFieldError(emailField, emailError, "Cet email est déjà utilisé.");
                return;
            }

            User user = new User(email, password, username, telephone);
            userService.ajouter(user);

            loadPage("/fxml/login.fxml", event, "Connexion");

        } catch (Exception e) {
            e.printStackTrace();
            showFieldError(null, termsError, "Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        loadPage("/fxml/login.fxml", event, "Connexion");
    }

    // ===== Helpers =====

    private void showFieldError(Control field, Label errorLabel, String message) {
        if (field != null) {
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8;");
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

    private void loadPage(String fxmlPath, ActionEvent event, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1500, 900);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}