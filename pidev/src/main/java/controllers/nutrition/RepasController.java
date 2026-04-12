package controllers.nutrition;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.PlanNutrition;
import model.Repas;
import service.PlanNutritionService;
import service.RepasService;

import java.sql.SQLException;
import java.sql.Timestamp;

public class RepasController {

    @FXML private ComboBox<PlanNutrition> planNutritionComboBox;
    @FXML private ComboBox<String> typeRepasComboBox;
    @FXML private TextField caloriesField;
    @FXML private TableView<Repas> repasTableView;
    @FXML private TableColumn<Repas, String> colType;
    @FXML private TableColumn<Repas, Float> colCalories;

    private final RepasService repasService = new RepasService();
    private final PlanNutritionService planNutritionService = new PlanNutritionService();
    private ObservableList<Repas> repasList;

    @FXML
    public void initialize() {
        colType.setCellValueFactory(new PropertyValueFactory<>("typeRepas"));
        colCalories.setCellValueFactory(new PropertyValueFactory<>("calories"));
        
        loadPlanNutritionData();

        planNutritionComboBox.setConverter(new StringConverter<PlanNutrition>() {
            @Override
            public String toString(PlanNutrition object) {
                if (object == null) return null;
                return object.getObjectif() + " (ID: " + object.getId() + ")";
            }

            @Override
            public PlanNutrition fromString(String string) {
                return null;
            }
        });

        planNutritionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateTypeRepasRecommendations(newVal.getObjectif());
            }
        });

        repasTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Populate forms
                for(PlanNutrition p : planNutritionComboBox.getItems()) {
                    if (p.getId() == newSelection.getPlanNutritionId()) {
                        planNutritionComboBox.setValue(p);
                        break;
                    }
                }
                typeRepasComboBox.setValue(newSelection.getTypeRepas());
                caloriesField.setText(String.valueOf(newSelection.getCalories()));
            }
        });

        afficher();
    }

    private void loadPlanNutritionData() {
        try {
            ObservableList<PlanNutrition> plans = FXCollections.observableArrayList(planNutritionService.afficher());
            planNutritionComboBox.setItems(plans);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTypeRepasRecommendations(String objectif) {
        ObservableList<String> types = FXCollections.observableArrayList();
        if (objectif == null) objectif = "";
        
        switch (objectif.toLowerCase()) {
            case "perte de poids":
                types.addAll("Repas riche en fibres", "Salade verte", "Repas faible calorie");
                break;
            case "prise de masse":
                types.addAll("Repas riche en protéines", "Collation protéinée");
                break;
            case "détox":
                types.addAll("Jus détox", "Soupe aux légumes");
                break;
            case "maintien":
                types.addAll("Repas équilibré", "Repas standard");
                break;
            case "régime spécial":
                types.addAll("Repas sans gluten", "Repas keto", "Repas vegan");
                break;
            case "performance sportive":
                types.addAll("Repas riche en glucides", "Hypercalorique");
                break;
            default:
                types.addAll("Repas standard");
                break;
        }
        typeRepasComboBox.setItems(types);
    }

    @FXML
    public void ajouter(ActionEvent event) {
        if (!isRepasValid()) return;

        try {

            Repas repas = new Repas();
            repas.setTypeRepas(typeRepasComboBox.getValue() != null ? typeRepasComboBox.getValue() : "");
            repas.setCalories(Float.parseFloat(caloriesField.getText().isEmpty() ? "0" : caloriesField.getText()));
            repas.setProteines(0f); // dummy
            repas.setGlucides(0f); // dummy
            repas.setLipides(0f); // dummy
            repas.setPortionSize("100g"); // dummy
            repas.setBarcode("000000"); // dummy
            repas.setDescription(""); // dummy
            repas.setDateRepas(new Timestamp(System.currentTimeMillis()));
            repas.setPlanNutritionId(planNutritionComboBox.getValue().getId());
            
            repasService.ajouter(repas);
            afficher();
            clearFields();
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void modifier(ActionEvent event) {
        Repas selectedItem = repasTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (!isRepasValid()) return;

            try {
                if (planNutritionComboBox.getValue() != null) {
                    selectedItem.setPlanNutritionId(planNutritionComboBox.getValue().getId());
                }
                selectedItem.setTypeRepas(typeRepasComboBox.getValue() != null ? typeRepasComboBox.getValue() : "");
                selectedItem.setCalories(Float.parseFloat(caloriesField.getText().isEmpty() ? "0" : caloriesField.getText()));
                
                repasService.modifier(selectedItem);
                afficher();
                clearFields();
                repasTableView.getSelectionModel().clearSelection();
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
                showAlert("Erreur lors de la modification: " + e.getMessage());
            }
        } else {
            showAlert("Veuillez sélectionner un repas à modifier.");
        }
    }

    @FXML
    public void supprimer(ActionEvent event) {
        Repas selectedItem = repasTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                repasService.supprimer(selectedItem.getId());
                afficher();
                clearFields();
                repasTableView.getSelectionModel().clearSelection();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur lors de la suppression: " + e.getMessage());
            }
        } else {
            showAlert("Veuillez sélectionner un repas à supprimer.");
        }
    }

    public void afficher() {
        try {
            repasList = FXCollections.observableArrayList(repasService.afficher());
            repasTableView.setItems(repasList);
            repasTableView.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur d'affichage: " + e.getMessage());
        }
    }

    private void clearFields() {
        planNutritionComboBox.setValue(null);
        typeRepasComboBox.setValue(null);
        caloriesField.clear();
    }

    private boolean isRepasValid() {
        if (planNutritionComboBox.getValue() == null) {
            showAlert("Veuillez sélectionner un Plan Nutrition.");
            return false;
        }
        if (typeRepasComboBox.getValue() == null || typeRepasComboBox.getValue().trim().isEmpty()) {
            showAlert("Veuillez sélectionner ou saisir un type de repas.");
            return false;
        }
        if (caloriesField.getText() == null || caloriesField.getText().trim().isEmpty()) {
            showAlert("Veuillez saisir les calories.");
            return false;
        }
        try {
            float cal = Float.parseFloat(caloriesField.getText().trim());
            if (cal < 0) {
                showAlert("Les calories ne peuvent pas être négatives.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Les calories doivent être un nombre valide (ex: 200.5).");
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
