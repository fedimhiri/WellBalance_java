package org.example.wellbalance.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.wellbalance.models.TypeRendezVous;
import org.example.wellbalance.services.TypeRendezVousService;

public class AjouterTypeRendezVousController extends BaseAdminController {

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

    @FXML
    public void ajouterTypeRendezVous() {
        if (!validateFields()) return;

        TypeRendezVous t = new TypeRendezVous();
        t.setLibelle(libelleField.getText().trim());
        t.setDescription(descriptionArea.getText().trim());
        t.setDuree(Integer.parseInt(dureeField.getText().trim()));
        t.setPrix(Double.parseDouble(prixField.getText().trim()));
        t.setCategorie(categorieField.getText().trim());

        boolean inserted = service.ajouter(t);

        if (inserted) {
            showSuccess("Type de rendez-vous ajouté avec succès.");
            clearFields();
        } else {
            showAlert("L'ajout a échoué. Vérifiez la connexion et l'existence de la table type_rendezvous.");
        }
    }

    @FXML
    public void clearFields() {
        libelleField.clear();
        descriptionArea.clear();
        dureeField.clear();
        prixField.clear();
        categorieField.clear();
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
}
