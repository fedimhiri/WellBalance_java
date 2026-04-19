package org.example.wellbalance.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import org.example.wellbalance.models.RendezVous;
import org.example.wellbalance.models.ResultatRecommandation;
import org.example.wellbalance.models.TypeRendezVous;
import org.example.wellbalance.services.RendezVousService;
import org.example.wellbalance.services.ServiceRecommandationRendezVous;
import org.example.wellbalance.services.TypeRendezVousService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AjouterRendezVousController extends BaseAdminController {

    @FXML
    private DatePicker datePicker;
    @FXML
    private Spinner<Integer> heureSpinner;
    @FXML
    private Spinner<Integer> minuteSpinner;
    @FXML
    private TextArea remarqueArea;
    @FXML
    private ComboBox<TypeRendezVous> comboType;
    @FXML
    private Label recommandationLabel;
    @FXML
    private Label creneauxDisponiblesLabel;

    private final RendezVousService rendezVousService = new RendezVousService();
    private final ServiceRecommandationRendezVous serviceRecommandation =
            new ServiceRecommandationRendezVous(rendezVousService);
    private final TypeRendezVousService typeRendezVousService = new TypeRendezVousService();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        comboType.setItems(FXCollections.observableArrayList(typeRendezVousService.afficher()));
        configureTimeControls();
        installRecommendationListeners();
        afficherRecommandation();
    }

    @FXML
    public void ajouterRendezVous() {
        if (!validateFields()) {
            return;
        }

        LocalTime heureSelectionnee = buildSelectedTime();
        ResultatRecommandation recommandation =
                serviceRecommandation.analyserCreneau(datePicker.getValue(), heureSelectionnee);
        afficherRecommandation(recommandation);

        if (!recommandation.isEstValide() || !recommandation.isEstDisponible()) {
            showAlert(formatRecommendationMessage(recommandation));
            return;
        }

        RendezVous r = new RendezVous();
        r.setDateRdv(datePicker.getValue());
        r.setHeureRdv(heureSelectionnee);
        r.setStatut(RendezVous.STATUT_EN_COURS);
        r.setRemarque(remarqueArea.getText().trim());
        r.setTypeRendezVous(comboType.getValue());

        rendezVousService.ajouter(r);
        showSuccess("Rendez-vous ajoute avec succes.");
        clearFields();
    }

    @FXML
    public void clearFields() {
        datePicker.setValue(null);
        heureSpinner.getValueFactory().setValue(9);
        minuteSpinner.getValueFactory().setValue(0);
        remarqueArea.clear();
        comboType.setValue(null);
        afficherRecommandation();
    }

    private void configureTimeControls() {
        heureSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));
        heureSpinner.setEditable(true);
        minuteSpinner.setEditable(true);
    }

    private void installRecommendationListeners() {
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> afficherRecommandation());
        heureSpinner.valueProperty().addListener((obs, oldVal, newVal) -> afficherRecommandation());
        minuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> afficherRecommandation());
    }

    private LocalTime buildSelectedTime() {
        return LocalTime.of(heureSpinner.getValue(), minuteSpinner.getValue());
    }

    private void afficherRecommandation() {
        if (datePicker.getValue() == null || heureSpinner.getValue() == null || minuteSpinner.getValue() == null) {
            recommandationLabel.setText("Choisissez une date et une heure pour obtenir une recommandation.");
            updateRecommendationStyle("neutre");
            creneauxDisponiblesLabel.setText("Creneaux autorises : 09:00 a 14:30, toutes les 30 minutes.");
            return;
        }

        afficherRecommandation(serviceRecommandation.analyserCreneau(datePicker.getValue(), buildSelectedTime()));
    }

    private void afficherRecommandation(ResultatRecommandation recommandation) {
        recommandationLabel.setText(formatRecommendationMessage(recommandation));
        updateRecommendationStyle(recommandation.getStatut());
        creneauxDisponiblesLabel.setText(formatCreneauxDisponibles(datePicker.getValue()));
    }

    private String formatRecommendationMessage(ResultatRecommandation recommandation) {
        StringBuilder message = new StringBuilder();
        message.append(recommandation.getMessage());
        message.append(" Score : ").append(recommandation.getScore()).append("/100.");

        if (recommandation.getCreneauPropose() != null) {
            message.append(" Alternative : ")
                    .append(recommandation.getCreneauPropose().format(timeFormatter))
                    .append(".");
        }

        return message.toString();
    }

    private String formatCreneauxDisponibles(LocalDate date) {
        if (date == null) {
            return "Creneaux disponibles : selectionnez une date.";
        }

        String creneaux = serviceRecommandation.recupererCreneauxDisponibles(date).stream()
                .map(timeFormatter::format)
                .reduce((a, b) -> a + ", " + b)
                .orElse("aucun");

        return "Creneaux disponibles : " + creneaux;
    }

    private void updateRecommendationStyle(String statut) {
        recommandationLabel.getStyleClass().removeAll(
                "recommendation-neutral",
                "recommendation-success",
                "recommendation-warning",
                "recommendation-danger"
        );

        String styleClass = switch (statut == null ? "" : statut) {
            case "recommande", "acceptable" -> "recommendation-success";
            case "deconseille" -> "recommendation-warning";
            case "occupe", "invalide" -> "recommendation-danger";
            default -> "recommendation-neutral";
        };
        recommandationLabel.getStyleClass().add(styleClass);
    }

    private boolean validateFields() {
        if (datePicker.getValue() == null) {
            showAlert("La date est obligatoire.");
            return false;
        }
        if (datePicker.getValue().isBefore(LocalDate.now())) {
            showAlert("La date ne doit pas etre dans le passe.");
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
        alert.setTitle("Succes");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
