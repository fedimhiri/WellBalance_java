package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.models.User;
import tn.esprit.services.UserService;
import tn.esprit.util.SessionManager;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private UserService userService = new UserService();
    private SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        // Clear error label on start
        errorLabel.setVisible(false);

        // Add enter key handler
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        // Tentative de connexion
        User user = userService.login(email, password);

        if (user != null) {
            // Connexion reussie
            sessionManager.login(user);
            navigateToMessenger(user);
        } else {
            showError("Email ou mot de passe incorrect");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void navigateToMessenger(User user) {
        try {
            FXMLLoader loader;
            String title;

            if (user.isDoctor()) {
                // Navigate to admin messenger (doctor view)
                loader = new FXMLLoader(getClass().getResource("/AdminMessenger.fxml"));
                title = "Messagerie - Espace Docteur";
            } else {
                // Navigate to patient messenger
                loader = new FXMLLoader(getClass().getResource("/PatientMessenger.fxml"));
                title = "Messagerie - Espace Patient";
            }

            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de l'interface");
        }
    }
}
