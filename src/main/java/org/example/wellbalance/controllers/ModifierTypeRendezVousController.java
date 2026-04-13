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

public class ModifierTypeRendezVousController {

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

    @FXML
    private TextField libelleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField dureeField;
    @FXML
    private TextField prixField;
    @FXML
    private TextField categorieField;

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

        typeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedType = newVal;
                libelleField.setText(newVal.getLibelle());
                descriptionArea.setText(newVal.getDescription());
                dureeField.setText(String.valueOf(newVal.getDuree()));
                prixField.setText(String.valueOf(newVal.getPrix()));
                categorieField.setText(newVal.getCategorie());
            }
        });
    }

    @FXML
    public void loadData() {
        typeTable.setItems(FXCollections.observableArrayList(service.afficher()));
    }

    @FXML
    public void modifierTypeRendezVous() {
        if (selectedType == null) {
            showAlert("Veuillez sélectionner un type de rendez-vous.");
            return;
        }

        if (!validateFields()) return;

        selectedType.setLibelle(libelleField.getText().trim());
        selectedType.setDescription(descriptionArea.getText().trim());
        selectedType.setDuree(Integer.parseInt(dureeField.getText().trim()));
        selectedType.setPrix(Double.parseDouble(prixField.getText().trim()));
        selectedType.setCategorie(categorieField.getText().trim());

        service.modifier(selectedType);
        showSuccess("Type de rendez-vous modifié avec succès.");
        clearFields();
        loadData();
    }

    @FXML
    public void clearFields() {
        libelleField.clear();
        descriptionArea.clear();
        dureeField.clear();
        prixField.clear();
        categorieField.clear();
        selectedType = null;
        typeTable.getSelectionModel().clearSelection();
    }

    private boolean validateFields() {
        if (libelleField.getText().trim().isEmpty()) {
            showAlert("Le libellé est obligatoire.");
            return false;
        }
        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert("La description est obligatoire.");
            return false;
        }
        if (!dureeField.getText().trim().matches("\\d+")) {
            showAlert("La durée doit être un nombre entier positif.");
            return false;
        }
        if (!prixField.getText().trim().matches("\\d+(\\.\\d+)?")) {
            showAlert("Le prix est invalide.");
            return false;
        }
        if (categorieField.getText().trim().isEmpty()) {
            showAlert("La catégorie est obligatoire.");
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