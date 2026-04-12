package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
    }

    @FXML
    public void handleSave() {
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (user == null) {
            showError("Aucun utilisateur chargé.");
            return;
        }

        if (email.isEmpty() || username.isEmpty() || telephone.isEmpty()) {
            showError("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        if (!password.isEmpty() && !password.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

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