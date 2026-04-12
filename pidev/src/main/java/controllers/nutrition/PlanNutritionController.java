package controllers.nutrition;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.PlanNutrition;
import service.PlanNutritionService;

import java.sql.SQLException;
import java.sql.Timestamp;

public class PlanNutritionController {

    @FXML private ComboBox<String> objectifComboBox;
    @FXML private TextField periodeField;
    @FXML private TextArea descriptionArea;
    @FXML private TableView<PlanNutrition> planTableView;
    @FXML private TableColumn<PlanNutrition, String> colObjectif;
    @FXML private TableColumn<PlanNutrition, String> colPeriode;
    @FXML private TableColumn<PlanNutrition, String> colDescription;
    
    private final PlanNutritionService planNutritionService = new PlanNutritionService();
    private ObservableList<PlanNutrition> plansList;

    @FXML
    public void initialize() {
        objectifComboBox.setItems(FXCollections.observableArrayList(
            "Perte de poids", "Prise de masse", "Détox", "Maintien", "Régime spécial", "Performance sportive"
        ));

        colObjectif.setCellValueFactory(new PropertyValueFactory<>("objectif"));
        colPeriode.setCellValueFactory(new PropertyValueFactory<>("periode"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        planTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Populate fields when a row is selected to fix the modification workflow
                objectifComboBox.setValue(newSelection.getObjectif());
                periodeField.setText(newSelection.getPeriode());
                descriptionArea.setText(newSelection.getDescription());
            }
        });
        
        afficher();
    }

    @FXML
    public void ajouter(ActionEvent event) {
        if (!isPlanValid()) return;

        try {
            PlanNutrition plan = new PlanNutrition();
            plan.setObjectif(objectifComboBox.getValue().trim());
            plan.setPeriode(periodeField.getText().trim());
            plan.setDescription(descriptionArea.getText().trim());
            plan.setDateDebut(new Timestamp(System.currentTimeMillis()));
            plan.setDateFin(new Timestamp(System.currentTimeMillis() + 864000000L)); // +10 days dummy
            plan.setUserId(1); // dummy
            plan.setNutritionnisteId(1); // dummy
            
            planNutritionService.ajouter(plan);
            afficher();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void modifier(ActionEvent event) {
        PlanNutrition selectedItem = planTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (!isPlanValid()) return;

            try {
                selectedItem.setObjectif(objectifComboBox.getValue().trim());
                selectedItem.setPeriode(periodeField.getText().trim());
                selectedItem.setDescription(descriptionArea.getText().trim());
                
                planNutritionService.modifier(selectedItem);
                afficher();
                clearFields();
                planTableView.getSelectionModel().clearSelection();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur lors de la modification: " + e.getMessage());
            }
        } else {
            showAlert("Veuillez sélectionner un plan à modifier.");
        }
    }

    @FXML
    public void supprimer(ActionEvent event) {
        PlanNutrition selectedItem = planTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                planNutritionService.supprimer(selectedItem.getId());
                afficher();
                clearFields();
                planTableView.getSelectionModel().clearSelection();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur lors de la suppression: " + e.getMessage());
            }
        } else {
            showAlert("Veuillez sélectionner un plan à supprimer.");
        }
    }

    public void afficher() {
        try {
            plansList = FXCollections.observableArrayList(planNutritionService.afficher());
            planTableView.setItems(plansList);
            planTableView.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur d'affichage: " + e.getMessage());
        }
    }

    private void clearFields() {
        objectifComboBox.setValue(null);
        periodeField.clear();
        descriptionArea.clear();
    }

    private boolean isPlanValid() {
        if (objectifComboBox.getValue() == null || objectifComboBox.getValue().trim().isEmpty()) {
            showAlert("Veuillez sélectionner un objectif.");
            return false;
        }
        
        String periodeStr = periodeField.getText() != null ? periodeField.getText().trim() : "";
        if (periodeStr.isEmpty()) {
            showAlert("Veuillez saisir une période valide.");
            return false;
        }
        
        try {
            int periode = Integer.parseInt(periodeStr);
            if (periode <= 0) {
                showAlert("La période doit être un nombre strictement positif (ex: 14 pour 14 jours).");
                return false;
            }
            // Update the text to string inside the field implicitly passing validation
            periodeField.setText(String.valueOf(periode));
        } catch (NumberFormatException e) {
            showAlert("La période doit être un nombre valide (ex: 14). Ne mettez pas de texte.");
            return false;
        }

        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            showAlert("Veuillez saisir une description.");
            return false;
        }
        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
