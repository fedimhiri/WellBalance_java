package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import service.UserSessionService;
import utils.Session;

public class FrontController {

    @FXML
    private Label welcomeLabel;

    private final UserSessionService userSessionService = new UserSessionService();

    @FXML
    public void initialize() {
        if (welcomeLabel != null && Session.getCurrentUser() != null) {
            welcomeLabel.setText("Bienvenue " + Session.getCurrentUser().getUsername());
        }
    }

    @FXML
    public void goToEditProfile(ActionEvent event) {
        loadPage("/fxml/edit_profile_user.fxml", event, "Mon Profil");
    }

    @FXML
    public void logout(ActionEvent event) {
        try {
            UserSessionService userSessionService = new UserSessionService();

            if (Session.getCurrentSessionToken() != null) {
                userSessionService.closeSession(Session.getCurrentSessionToken());
            }

            Session.clear();
            loadPage("/fxml/login.fxml", event, "Connexion");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la déconnexion : " + e.getMessage());
        }
    }


    private void loadPage(String fxmlPath, ActionEvent event, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1400, 850);

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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page : " + e.getMessage());
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