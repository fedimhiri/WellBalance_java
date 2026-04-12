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

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField telephoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private CheckBox agreeTermsCheckBox;

    private final UserService userService = new UserService();

    @FXML
    public void handleRegister(ActionEvent event) {
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (email.isEmpty() || username.isEmpty() || telephone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs requis", "Veuillez remplir tous les champs.");
            return;
        }

        if (!telephone.matches("\\d{8}")) {
            showAlert(Alert.AlertType.WARNING, "Téléphone invalide", "Le téléphone doit contenir exactement 8 chiffres.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Mot de passe", "Les mots de passe ne correspondent pas.");
            return;
        }

        if (!agreeTermsCheckBox.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Conditions", "Vous devez accepter les conditions.");
            return;
        }

        try {
            User existingUser = userService.getUserByEmail(email);
            if (existingUser != null) {
                showAlert(Alert.AlertType.ERROR, "Email existant", "Un utilisateur avec cet email existe déjà.");
                return;
            }

            User user = new User(email, password, username, telephone);
            userService.ajouter(user);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte créé avec succès.");

            loadPage("/fxml/login.fxml", event, "Connexion");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        loadPage("/fxml/login.fxml", event, "Connexion");
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
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}