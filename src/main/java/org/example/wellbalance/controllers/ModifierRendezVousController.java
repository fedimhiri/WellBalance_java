package org.example.wellbalance.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

public class ModifierRendezVousController {

    @FXML
    private TableView<RendezVous> rendezVousTable;
    @FXML
    private TableColumn<RendezVous, Integer> colId;
    @FXML
    private TableColumn<RendezVous, LocalDate> colDate;
    @FXML
    private TableColumn<RendezVous, LocalTime> colHeure;
    @FXML
    private TableColumn<RendezVous, String> colStatut;
    @FXML
    private TableColumn<RendezVous, String> colRemarque;
    @FXML
    private TableColumn<RendezVous, String> colType;

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

    private RendezVous selectedRdv;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateRdv"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureRdv"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colRemarque.setCellValueFactory(new PropertyValueFactory<>("remarque"));
        colType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeRendezVous().getLibelle())
        );

        comboType.setItems(FXCollections.observableArrayList(typeRendezVousService.afficher()));
        loadData();

        rendezVousTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedRdv = newVal;
                datePicker.setValue(newVal.getDateRdv());
                heureField.setText(newVal.getHeureRdv().toString());
                statutField.setText(newVal.getStatut());
                remarqueArea.setText(newVal.getRemarque());
                comboType.setValue(newVal.getTypeRendezVous());
            }
        });
    }

    @FXML
    public void loadData() {
        rendezVousTable.setItems(FXCollections.observableArrayList(rendezVousService.afficher()));
    }

    @FXML
    public void modifierRendezVous() {
        if (selectedRdv == null) {
            showAlert("Veuillez sélectionner un rendez-vous.");
            return;
        }

        if (!validateFields()) return;

        selectedRdv.setDateRdv(datePicker.getValue());
        selectedRdv.setHeureRdv(LocalTime.parse(heureField.getText().trim()));
        selectedRdv.setStatut(statutField.getText().trim());
        selectedRdv.setRemarque(remarqueArea.getText().trim());
        selectedRdv.setTypeRendezVous(comboType.getValue());

        rendezVousService.modifier(selectedRdv);
        showSuccess("Rendez-vous modifié avec succès.");
        clearFields();
        loadData();
    }

    @FXML
    public void clearFields() {
        datePicker.setValue(null);
        heureField.clear();
        statutField.clear();
        remarqueArea.clear();
        comboType.setValue(null);
        selectedRdv = null;
        rendezVousTable.getSelectionModel().clearSelection();
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