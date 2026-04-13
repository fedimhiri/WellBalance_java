package org.example.wellbalance.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.wellbalance.models.RendezVous;
import org.example.wellbalance.models.TypeRendezVous;
import org.example.wellbalance.services.RendezVousService;
import org.example.wellbalance.services.TypeRendezVousService;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;

public class AjouterRendezVousController {

    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField heureField;
    @FXML
    private TextField statutField;
    @FXML
    private TextArea remarqueArea;
    @FXML
    private ComboBox<TypeRendezVous> comboType;

    private final RendezVousService rendezVousService = new RendezVousService();
    private final TypeRendezVousService typeRendezVousService = new TypeRendezVousService();

    @FXML
    public void initialize() {
        comboType.setItems(FXCollections.observableArrayList(typeRendezVousService.afficher()));
    }

    @FXML
    public void ajouterRendezVous() {
        if (!validateFields()) return;

        RendezVous r = new RendezVous();
        r.setDateRdv(datePicker.getValue());
        r.setHeureRdv(LocalTime.parse(heureField.getText().trim()));
        r.setStatut(statutField.getText().trim());
        r.setRemarque(remarqueArea.getText().trim());
        r.setTypeRendezVous(comboType.getValue());

        rendezVousService.ajouter(r);
        showSuccess("Rendez-vous ajouté avec succès.");
        clearFields();
    }

    @FXML
    public void clearFields() {
        datePicker.setValue(null);
        heureField.clear();
        statutField.clear();
        remarqueArea.clear();
        comboType.setValue(null);
    }

    private boolean validateFields() {
        if (datePicker.getValue() == null) {
            showAlert("La date est obligatoire.");
            return false;
        }
        if (datePicker.getValue().isBefore(LocalDate.now())) {
            showAlert("La date ne doit pas être dans le passé.");
            return false;
        }
        if (heureField.getText().trim().isEmpty()) {
            showAlert("L'heure est obligatoire.");
            return false;
        }
        if (!heureField.getText().trim().matches("^([01]\\d|2[0-3]):([0-5]\\d)$")) {
            showAlert("Format heure invalide. Utilisez HH:mm");
            return false;
        }
        if (statutField.getText().trim().isEmpty()) {
            showAlert("Le statut est obligatoire.");
            return false;
        }
        if (comboType.getValue() == null) {
            showAlert("Veuillez choisir un type de rendez-vous.");
            return false;
        }
        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    public void retourMenu(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/menu.fxml"));
        Scene scene = new Scene(loader.load(), 700, 400);

        URL cssUrl = getClass().getResource("/css/modern-style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("WellBalance - Menu");
        stage.show();
    }
}