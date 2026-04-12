package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.User;
import service.UserService;
import service.UserSessionService;
import utils.Session;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AdminDashboardController {

    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colTelephone;
    @FXML private TableColumn<User, String> colRoles;
    @FXML private TableColumn<User, String> colStatut;
    @FXML private TableColumn<User, Void> colActions;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        if (!Session.isAdmin()) {
            navigateTo("/fxml/login.fxml", "Connexion");
            return;
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colRoles.setCellValueFactory(new PropertyValueFactory<>("roles"));

        colStatut.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String statut = user.getIsBanned() == 1 ? "BANNI" : "ACTIF";
            return javafx.beans.binding.Bindings.createStringBinding(() -> statut);
        });

        addActionsButtons();
        loadUsers();
    }

    private void loadUsers() {
        try {
            List<User> usersList = userService.afficher();
            ObservableList<User> users = FXCollections.observableArrayList(usersList);
            tableUsers.setItems(users);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des utilisateurs : " + e.getMessage());
        }
    }

    private void addActionsButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnBanToggle = new Button();
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox pane = new HBox(8, btnModifier, btnBanToggle, btnSupprimer);

            {
                btnModifier.getStyleClass().add("btn-edit");
                btnSupprimer.getStyleClass().add("btn-delete");

                btnModifier.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    modifierUser(user);
                });

                btnBanToggle.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    toggleBanUser(user);
                });

                btnSupprimer.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    supprimerUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    btnBanToggle.getStyleClass().removeAll("btn-ban", "btn-unban");

                    if (user.getIsBanned() == 1) {
                        btnBanToggle.setText("Débannir");
                        btnBanToggle.getStyleClass().add("btn-unban");
                    } else {
                        btnBanToggle.setText("Bannir");
                        btnBanToggle.getStyleClass().add("btn-ban");
                    }

                    setGraphic(pane);
                }
            }
        });
    }

    private void modifierUser(User user) {
        try {
            Session.setCurrentUser(user);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_profile.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1400, 850);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );

            EditProfileController controller = loader.getController();
            controller.setOnUpdateSuccess(() -> loadPageFromNode("/fxml/admin_dashboard.fxml", tableUsers, "Admin Dashboard"));

            Stage stage = (Stage) tableUsers.getScene().getWindow();
            stage.setTitle("Modifier utilisateur");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification : " + e.getMessage());
        }
    }

    private void toggleBanUser(User user) {
        try {
            if (user.getIsBanned() == 1) {
                userService.changerStatutBan(user.getId(), 0);
                showAlert("Succès", "Utilisateur débanni avec succès.");
            } else {
                userService.changerStatutBan(user.getId(), 1);
                showAlert("Succès", "Utilisateur banni avec succès.");
            }
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du changement de statut : " + e.getMessage());
        }
    }

    private void supprimerUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer : " + user.getUsername() + " ?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                userService.supprimer(user.getId());
                loadUsers();
                showAlert("Succès", "Utilisateur supprimé avec succès.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    public void goToFrontOffice(javafx.event.ActionEvent event) {
        loadPageFromEvent("/fxml/front.fxml", event, "Front Office");
    }

    @FXML
    public void goToMyProfile(javafx.event.ActionEvent event) {
        loadPageFromEvent("/fxml/edit_profile.fxml", event, "Mon Profil");
    }

    private void loadPageFromEvent(String fxmlPath, javafx.event.ActionEvent event, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1400, 850);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );

            // Récupérer le Stage de manière robuste
            Stage stage = null;
            Node sourceNode = (Node) event.getSource();
            if (sourceNode.getScene() != null && sourceNode.getScene().getWindow() != null) {
                stage = (Stage) sourceNode.getScene().getWindow();
            } else {
                // Chercher le Stage actuel parmi les fenêtres ouvertes
                for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                    if (window instanceof Stage && window.isShowing()) {
                        stage = (Stage) window;
                        break;
                    }
                }
            }

            if (stage == null) {
                System.err.println("Aucun Stage trouvé pour la navigation");
                return;
            }

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    private void loadPageFromNode(String fxmlPath, Node sourceNode, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1400, 850);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page : " + e.getMessage());
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

            Stage stage = (Stage) tableUsers.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    @FXML
    public void logout() {
        try {
            UserSessionService userSessionService = new UserSessionService();

            if (Session.getCurrentSessionToken() != null) {
                userSessionService.closeSession(Session.getCurrentSessionToken());
            }

            Session.clear();
            navigateTo("/fxml/login.fxml", "Connexion");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la déconnexion : " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert.AlertType type = title.equalsIgnoreCase("Erreur")
                ? Alert.AlertType.ERROR
                : Alert.AlertType.INFORMATION;

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    public void goToSessions(ActionEvent event) {
        loadPageFromEvent("/fxml/user_sessions.fxml", event, "Sessions utilisateurs");
    }

    @FXML
    public void goToMessagerie(ActionEvent event) {
        loadPageFromEvent("/fxml/AdminMessenger.fxml", event, "Messagerie - Docteur");
    }
}