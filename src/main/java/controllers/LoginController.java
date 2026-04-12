package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import service.UserService;
import service.UserSessionService;
import utils.Session;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final UserService userService = new UserService();
    private final UserSessionService userSessionService = new UserSessionService();

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs requis", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            User user = userService.getUserByEmail(email);

            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Email ou mot de passe invalide.");
                return;
            }

            if (user.getIsBanned() == 1) {
                showAlert(Alert.AlertType.ERROR, "Compte banni", "Cet utilisateur est banni.");
                return;
            }

            boolean ok = userService.login(email, password);

            if (!ok) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Email ou mot de passe invalide.");
                return;
            }

            // Stocker l'utilisateur en session
            Session.setCurrentUser(user);

            // Créer une session en base et stocker le token
            String token = userSessionService.createSession(user);
            Session.setCurrentSessionToken(token);

            // Redirection selon le rôle
            String roles = user.getRoles();

            if (roles != null && roles.contains("ROLE_ADMIN")) {
                loadPage("/fxml/admin_dashboard.fxml", event, "Admin Dashboard");
            } else {
                loadPage("/fxml/front.fxml", event, "Front Office");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la connexion : " + e.getMessage());
        }
    }

    @FXML
    public void goToRegister(ActionEvent event) {
        loadPage("/fxml/register.fxml", event, "Créer un compte");
    }

    private void loadPage(String fxmlPath, ActionEvent event, String title) {
        try {
            if (getClass().getResource(fxmlPath) == null) {
                throw new Exception("Fichier FXML introuvable : " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1500, 900);

            if (getClass().getResource("/css/style.css") != null) {
                scene.getStylesheets().add(
                        getClass().getResource("/css/style.css").toExternalForm()
                );
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

            Session.startInactivityTimer(scene, stage);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger la page : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    public void goToForgotPassword(ActionEvent event) {
        loadPage("/fxml/forgot_password.fxml", event, "Mot de passe oublié");
    }
}