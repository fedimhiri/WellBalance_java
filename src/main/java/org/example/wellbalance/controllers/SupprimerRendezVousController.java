package org.example.wellbalance.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.wellbalance.models.RendezVous;
import org.example.wellbalance.services.RendezVousService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;

public class SupprimerRendezVousController {

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

    private final RendezVousService rendezVousService = new RendezVousService();
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

        loadData();

        rendezVousTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedRdv = newVal;
        });
    }

    @FXML
    public void loadData() {
        rendezVousTable.setItems(FXCollections.observableArrayList(rendezVousService.afficher()));
    }

    @FXML
    public void supprimerRendezVous() {
        if (selectedRdv == null) {
            showAlert("Veuillez sélectionner un rendez-vous.");
            return;
        }

        rendezVousService.supprimer(selectedRdv.getId());
        showSuccess("Rendez-vous supprimé avec succès.");
        loadData();
        selectedRdv = null;
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
}