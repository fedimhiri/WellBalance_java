package org.example.wellbalance.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.wellbalance.models.TypeRendezVous;
import org.example.wellbalance.services.TypeRendezVousService;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

public class SupprimerTypeRendezVousController {

    @FXML
    private TableView<TypeRendezVous> typeTable;
    @FXML
    private TableColumn<TypeRendezVous, Integer> colId;
    @FXML
    private TableColumn<TypeRendezVous, String> colLibelle;
    @FXML
    private TableColumn<TypeRendezVous, String> colDescription;
    @FXML
    private TableColumn<TypeRendezVous, Integer> colDuree;
    @FXML
    private TableColumn<TypeRendezVous, Double> colPrix;
    @FXML
    private TableColumn<TypeRendezVous, String> colCategorie;

    private final TypeRendezVousService service = new TypeRendezVousService();
    private TypeRendezVous selectedType;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLibelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        loadData();

        typeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> selectedType = newVal);
    }

    @FXML
    public void loadData() {
        typeTable.setItems(FXCollections.observableArrayList(service.afficher()));
    }

    @FXML
    public void supprimerTypeRendezVous() {
        if (selectedType == null) {
            showAlert("Veuillez sélectionner un type de rendez-vous.");
            return;
        }

        service.supprimer(selectedType.getId());
        showSuccess("Type de rendez-vous supprimé avec succès.");
        loadData();
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