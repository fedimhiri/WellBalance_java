package controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.User;
import service.GoogleAuthService;
import service.UserService;
import service.UserSessionService;
import utils.Session;
import utils.ThemeManager;

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

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statutFilter;

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label bannedUsersLabel;
    @FXML private Label adminUsersLabel;

    @FXML private BarChart<String, Number> usersBarChart;
    @FXML private Button themeToggleButton;

    private final UserService userService = new UserService();
    private ObservableList<User> masterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colRoles.setCellValueFactory(new PropertyValueFactory<>("roles"));

        colStatut.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String statut = user.getIsBanned() == 1 ? "BANNI" : "ACTIF";
            return Bindings.createStringBinding(() -> statut);
        });

        if (roleFilter != null) {
            roleFilter.setValue("Tous");
        }

        if (statutFilter != null) {
            statutFilter.setValue("Tous");
        }

        addActionsButtons();
        loadUsers();
        applyCurrentTheme();
    }

    private void loadUsers() {
        try {
            List<User> usersList = userService.afficher();
            masterList = FXCollections.observableArrayList(usersList);
            applyFilters();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des utilisateurs : " + e.getMessage());
        }
    }

    @FXML
    public void onSearch() {
        applyFilters();
    }

    @FXML
    public void onFilter() {
        applyFilters();
    }

    @FXML
    public void onReset() {
        if (searchField != null) {
            searchField.clear();
        }
        if (roleFilter != null) {
            roleFilter.setValue("Tous");
        }
        if (statutFilter != null) {
            statutFilter.setValue("Tous");
        }
        applyFilters();
    }

    private void applyFilters() {
        String search = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase()
                : "";

        String role = roleFilter != null && roleFilter.getValue() != null
                ? roleFilter.getValue()
                : "Tous";

        String statut = statutFilter != null && statutFilter.getValue() != null
                ? statutFilter.getValue()
                : "Tous";

        FilteredList<User> filtered = new FilteredList<>(masterList, user -> {
            boolean matchSearch = search.isEmpty()
                    || (user.getEmail() != null && user.getEmail().toLowerCase().contains(search))
                    || (user.getUsername() != null && user.getUsername().toLowerCase().contains(search))
                    || (user.getTelephone() != null && user.getTelephone().toLowerCase().contains(search));

            boolean matchRole = role.equals("Tous")
                    || (user.getRoles() != null && user.getRoles().contains(role));

            boolean matchStatut = statut.equals("Tous")
                    || (statut.equals("BANNI") && user.getIsBanned() == 1)
                    || (statut.equals("ACTIF") && user.getIsBanned() == 0);

            return matchSearch && matchRole && matchStatut;
        });

        SortedList<User> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tableUsers.comparatorProperty());
        tableUsers.setItems(sorted);

        updateStats(filtered);
    }

    private void updateStats(List<User> users) {
        int totalCount = users.size();

        long activeCount = users.stream()
                .filter(user -> user.getIsBanned() == 0)
                .count();

        long bannedCount = users.stream()
                .filter(user -> user.getIsBanned() == 1)
                .count();

        long adminCount = users.stream()
                .filter(user -> user.getRoles() != null && user.getRoles().contains("ROLE_ADMIN"))
                .count();

        if (totalUsersLabel != null) {
            totalUsersLabel.setText(String.valueOf(totalCount));
        }

        if (activeUsersLabel != null) {
            activeUsersLabel.setText(String.valueOf(activeCount));
        }

        if (bannedUsersLabel != null) {
            bannedUsersLabel.setText(String.valueOf(bannedCount));
        }

        if (adminUsersLabel != null) {
            adminUsersLabel.setText(String.valueOf(adminCount));
        }

        updateChart(totalCount, (int) activeCount, (int) bannedCount, (int) adminCount);
    }

    private void updateChart(int total, int actifs, int bannis, int admins) {
        if (usersBarChart == null) {
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Total", total));
        series.getData().add(new XYChart.Data<>("Actifs", actifs));
        series.getData().add(new XYChart.Data<>("Bannis", bannis));
        series.getData().add(new XYChart.Data<>("Admins", admins));

        usersBarChart.getData().clear();
        usersBarChart.getData().add(series);
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_user_admin.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1400, 850);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) tableUsers.getScene().getWindow();
            stage.setTitle("Modification utilisateur");
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
    public void goToFrontOffice(ActionEvent event) {
        loadPageFromEvent("/fxml/front.fxml", event, "Front Office");
    }

    @FXML
    public void goToMyProfile(ActionEvent event) {
        loadPageFromEvent("/fxml/edit_profile.fxml", event, "Mon Profil");
    }

    @FXML
    public void goToSessions(ActionEvent event) {
        loadPageFromEvent("/fxml/user_sessions.fxml", event, "Sessions utilisateurs");
    }

    @FXML
    public void toggleTheme() {
        ThemeManager.toggleTheme(themeToggleButton.getScene());
        updateThemeButtonText();
    }

    private void applyCurrentTheme() {
        if (themeToggleButton != null && themeToggleButton.getScene() != null) {
            ThemeManager.applyTheme(themeToggleButton.getScene());
            updateThemeButtonText();
        } else if (tableUsers != null) {
            // Fallback if scene is not yet attached
            tableUsers.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    ThemeManager.applyTheme(newScene);
                    updateThemeButtonText();
                }
            });
        }
    }

    private void updateThemeButtonText() {
        if (themeToggleButton != null) {
            if (ThemeManager.isDarkMode()) {
                themeToggleButton.setText("☀️ Mode Clair");
            } else {
                themeToggleButton.setText("🌙 Mode Sombre");
            }
        }
    }

    @FXML
    public void logout() {
        try {
            UserSessionService userSessionService = new UserSessionService();

            if (Session.getCurrentSessionToken() != null) {
                userSessionService.closeSession(Session.getCurrentSessionToken());
            }

            GoogleAuthService googleAuthService = new GoogleAuthService();
            googleAuthService.clearSavedGoogleSession();

            Session.clear();
            navigateTo("/fxml/login.fxml", "Connexion");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la déconnexion : " + e.getMessage());
        }
    }

    private void loadPageFromEvent(String fxmlPath, ActionEvent event, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1400, 850);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            ThemeManager.applyTheme(scene);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) tableUsers.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page : " + e.getMessage());
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
}