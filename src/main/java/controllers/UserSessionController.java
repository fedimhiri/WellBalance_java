package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.UserSession;
import service.UserSessionService;
import utils.Session;

import java.sql.SQLException;
import java.util.List;

public class UserSessionController {

    @FXML private TableView<UserSession> tableSessions;

    @FXML private TableColumn<UserSession, Integer> colId;
    @FXML private TableColumn<UserSession, String> colUsername;
    @FXML private TableColumn<UserSession, String> colRole;
    @FXML private TableColumn<UserSession, Object> colLoginTime;
    @FXML private TableColumn<UserSession, Object> colLogoutTime;
    @FXML private TableColumn<UserSession, String> colStatus;
    @FXML private TableColumn<UserSession, String> colIp;
    @FXML private TableColumn<UserSession, Void> colActions;

    private final UserSessionService sessionService = new UserSessionService();

    @FXML
    public void initialize() {
        if (!Session.isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé", "Vous n'avez pas accès à cette page.");
            return;
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colLoginTime.setCellValueFactory(new PropertyValueFactory<>("loginTime"));
        colLogoutTime.setCellValueFactory(new PropertyValueFactory<>("logoutTime"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("sessionStatus"));
        colIp.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));

        addActionsButtons();
        loadSessions();
    }

    private void loadSessions() {
        try {
            List<UserSession> sessions = sessionService.getAllSessions();
            tableSessions.setItems(FXCollections.observableArrayList(sessions));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les sessions : " + e.getMessage());
        }
    }

    private void addActionsButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnSupprimer = new Button("Supprimer");

            {
                btnSupprimer.getStyleClass().add("btn-delete");

                btnSupprimer.setOnAction(event -> {
                    UserSession session = getTableView().getItems().get(getIndex());
                    supprimerSession(session);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnSupprimer);
                }
            }
        });
    }

    private void supprimerSession(UserSession session) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer cette session de " + session.getUsername() + " ?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                sessionService.supprimerSession(session.getId());
                loadSessions();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Session supprimée avec succès.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la session : " + e.getMessage());
            }
        }
    }

    @FXML
    public void goToDashboard(ActionEvent event) {
        loadPage("/fxml/admin_dashboard.fxml", event, "Admin Dashboard");
    }

    @FXML
    public void goToFrontOffice(ActionEvent event) {
        loadPage("/fxml/front.fxml", event, "Front Office");
    }

    @FXML
    public void goToMyProfile(ActionEvent event) {
        loadPage("/fxml/edit_profile.fxml", event, "Mon Profil");
    }

    @FXML
    public void logout(ActionEvent event) {
        try {
            if (Session.getCurrentSessionToken() != null) {
                sessionService.closeSession(Session.getCurrentSessionToken());
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