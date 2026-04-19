package org.example.wellbalance.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.wellbalance.models.TypeRendezVous;
import org.example.wellbalance.services.TypeRendezVousService;
import org.example.wellbalance.utils.TableFilterSupport;

import java.util.Comparator;

public class ModifierTypeRendezVousController extends BaseAdminController {

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
    private TextField searchField;
    @FXML
    private ComboBox<String> categorieFilter;
    @FXML
    private ComboBox<String> sortCombo;
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
    private TableFilterSupport<TypeRendezVous> tableSupport;
    private TypeRendezVous selectedType;

    @FXML
    public void initialize() {
        tableSupport = new TableFilterSupport<>(typeTable);
        configureColumns();
        configureFilters();
        installListeners();
        loadData();

        typeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedType = newVal;
            if (newVal != null) {
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
        tableSupport.setItems(service.afficher());
        categorieFilter.getItems().setAll("Toutes");
        tableSupport.getSource().stream()
                .map(TypeRendezVous::getCategorie)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(categorieFilter.getItems()::add);
        if (categorieFilter.getValue() == null) {
            categorieFilter.setValue("Toutes");
        }
        if (sortCombo.getValue() == null) {
            sortCombo.setValue("Libelle A-Z");
        }
        refreshFilters();
        applySort();
    }

    @FXML
    public void modifierTypeRendezVous() {
        if (selectedType == null) {
            showAlert("Veuillez selectionner un type de rendez-vous.");
            return;
        }

        if (!validateFields()) {
            return;
        }

        selectedType.setLibelle(libelleField.getText().trim());
        selectedType.setDescription(descriptionArea.getText().trim());
        selectedType.setDuree(Integer.parseInt(dureeField.getText().trim()));
        selectedType.setPrix(Double.parseDouble(prixField.getText().trim()));
        selectedType.setCategorie(categorieField.getText().trim());

        service.modifier(selectedType);
        showSuccess("Type de rendez-vous modifie avec succes.");
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

    @FXML
    public void resetFilters() {
        searchField.clear();
        categorieFilter.setValue("Toutes");
        sortCombo.setValue("Libelle A-Z");
        refreshFilters();
        applySort();
    }

    private void configureColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLibelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colCategorie.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("badge-cell", "badge-purple");
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    return;
                }
                setText(item);
                getStyleClass().addAll("badge-cell", "badge-purple");
            }
        });
    }

    private void configureFilters() {
        categorieFilter.getItems().setAll("Toutes");
        sortCombo.getItems().setAll(
                "Libelle A-Z",
                "Libelle Z-A",
                "Prix croissant",
                "Prix decroissant",
                "Duree croissante",
                "Duree decroissante"
        );
        categorieFilter.setValue("Toutes");
        sortCombo.setValue("Libelle A-Z");
    }

    private void installListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshFilters());
        categorieFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshFilters());
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySort());
    }

    private void refreshFilters() {
        String search = searchField.getText();
        String categorie = categorieFilter.getValue();

        tableSupport.apply(type ->
                matchesSearch(type, search) && matchesCategorie(type, categorie)
        );
    }

    private boolean matchesSearch(TypeRendezVous type, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String searchable = String.join(" ",
                safe(type.getLibelle()),
                safe(type.getDescription()),
                safe(type.getCategorie()),
                String.valueOf(type.getDuree()),
                String.valueOf(type.getPrix())
        );
        return TableFilterSupport.containsNormalized(searchable, search);
    }

    private boolean matchesCategorie(TypeRendezVous type, String categorie) {
        return categorie == null || "Toutes".equals(categorie) || categorie.equalsIgnoreCase(type.getCategorie());
    }

    private void applySort() {
        Comparator<TypeRendezVous> comparator = switch (sortCombo.getValue()) {
            case "Libelle Z-A" -> Comparator.comparing(TypeRendezVous::getLibelle, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Prix croissant" -> Comparator.comparingDouble(TypeRendezVous::getPrix);
            case "Prix decroissant" -> Comparator.comparingDouble(TypeRendezVous::getPrix).reversed();
            case "Duree croissante" -> Comparator.comparingInt(TypeRendezVous::getDuree);
            case "Duree decroissante" -> Comparator.comparingInt(TypeRendezVous::getDuree).reversed();
            default -> Comparator.comparing(TypeRendezVous::getLibelle, String.CASE_INSENSITIVE_ORDER);
        };
        tableSupport.sortWith(comparator);
    }

    private boolean validateFields() {
        if (libelleField.getText().trim().isEmpty()) {
            showAlert("Le libelle est obligatoire.");
            return false;
        }
        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert("La description est obligatoire.");
            return false;
        }
        if (!dureeField.getText().trim().matches("\\d+")) {
            showAlert("La duree doit etre un nombre entier positif.");
            return false;
        }
        if (!prixField.getText().trim().matches("\\d+(\\.\\d+)?")) {
            showAlert("Le prix est invalide.");
            return false;
        }
        if (categorieField.getText().trim().isEmpty()) {
            showAlert("La categorie est obligatoire.");
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
        alert.setTitle("Succes");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
