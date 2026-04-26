package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import service.UserService;
import utils.Session;

import java.io.IOException;
import java.sql.SQLException;

public class EditUserAdminController {

    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private TextField telephoneField;
    @FXML private ComboBox<String> roleComboBox;

    private final UserService userService = new UserService();
    private User selectedUser;

    @FXML
    public void initialize() {
        selectedUser = Session.getCurrentUser();

        if (selectedUser != null) {
            emailField.setText(selectedUser.getEmail());
            usernameField.setText(selectedUser.getUsername());
            telephoneField.setText(selectedUser.getTelephone());

            String role = selectedUser.getRoles();
            if (role != null) {
                if (role.contains("ROLE_ADMIN")) {
                    roleComboBox.setValue("ROLE_ADMIN");
                } else if (role.contains("ROLE_NUTRITIONNISTE")) {
                    roleComboBox.setValue("ROLE_NUTRITIONNISTE");
                } else if (role.contains("ROLE_COACHSPORTIF")) {
                    roleComboBox.setValue("ROLE_COACHSPORTIF");
                } else if (role.contains("ROLE_MEDECIN")) {
                    roleComboBox.setValue("ROLE_MEDECIN");
                } else {
                    roleComboBox.setValue("ROLE_USER");
                }
            }
        }
    }

    @FXML
    public void handleUpdateUser(ActionEvent event) {
        if (selectedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur sélectionné.");
            return;
        }

        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String role = roleComboBox.getValue();

        if (email.isEmpty() || username.isEmpty() || telephone.isEmpty() || role == null || role.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs requis", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            selectedUser.setEmail(email);
            selectedUser.setUsername(username);
            selectedUser.setTelephone(telephone);

            // important : format JSON pour Symfony
            selectedUser.setRoles("[\"" + role + "\"]");

            userService.modifier(selectedUser);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur modifié avec succès.");
            goBackToAdminDashboard(event);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification : " + e.getMessage());
        }
    }

    @FXML
    public void goBackToAdminDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 850);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("Admin Dashboard");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner vers le dashboard.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}