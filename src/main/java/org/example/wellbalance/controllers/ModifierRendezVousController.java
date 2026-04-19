package org.example.wellbalance.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.example.wellbalance.models.RendezVous;
import org.example.wellbalance.models.ResultatRecommandation;
import org.example.wellbalance.models.TypeRendezVous;
import org.example.wellbalance.services.EmailService;
import org.example.wellbalance.services.RendezVousService;
import org.example.wellbalance.services.ServiceRecommandationRendezVous;
import org.example.wellbalance.services.TypeRendezVousService;
import org.example.wellbalance.utils.TableFilterSupport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.time.format.DateTimeFormatter;

public class ModifierRendezVousController extends BaseAdminController {

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
    private TableColumn<RendezVous, Void> colActions;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statutFilter;
    @FXML
    private ComboBox<String> typeFilter;
    @FXML
    private ComboBox<String> sortCombo;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Spinner<Integer> heureSpinner;
    @FXML
    private Spinner<Integer> minuteSpinner;
    @FXML
    private TextField statutField;
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
    private final EmailService emailService = new EmailService();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private TableFilterSupport<RendezVous> tableSupport;
    private RendezVous selectedRdv;

    @FXML
    public void initialize() {
        tableSupport = new TableFilterSupport<>(rendezVousTable);
        configureColumns();
        configureFilters();
        configureTimeControls();
        comboType.setItems(FXCollections.observableArrayList(typeRendezVousService.afficher()));
        statutField.setEditable(false);
        installListeners();
        loadData();

        rendezVousTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedRdv = newVal;
            if (newVal != null) {
                datePicker.setValue(newVal.getDateRdv());
                heureSpinner.getValueFactory().setValue(newVal.getHeureRdv().getHour());
                minuteSpinner.getValueFactory().setValue(newVal.getHeureRdv().getMinute());
                statutField.setText(newVal.getStatut());
                remarqueArea.setText(newVal.getRemarque());
                comboType.setValue(newVal.getTypeRendezVous());
                afficherRecommandation();
            }
        });
    }

    @FXML
    public void loadData() {
        tableSupport.setItems(rendezVousService.afficher());
        statutFilter.getItems().setAll(
                "Tous",
                RendezVous.STATUT_EN_COURS,
                RendezVous.STATUT_ACCEPTE,
                RendezVous.STATUT_REFUSE
        );
        typeFilter.getItems().setAll("Tous");
        tableSupport.getSource().stream()
                .map(r -> r.getTypeRendezVous().getLibelle())
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(typeFilter.getItems()::add);
        if (statutFilter.getValue() == null) {
            statutFilter.setValue("Tous");
        }
        if (typeFilter.getValue() == null) {
            typeFilter.setValue("Tous");
        }
        if (sortCombo.getValue() == null) {
            sortCombo.setValue("Date croissante");
        }
        refreshFilters();
        applySort();
    }

    @FXML
    public void modifierRendezVous() {
        if (selectedRdv == null) {
            showAlert("Veuillez selectionner un rendez-vous.");
            return;
        }

        if (!validateFields()) {
            return;
        }

        LocalTime heureSelectionnee = buildSelectedTime();
        ResultatRecommandation recommandation =
                serviceRecommandation.analyserCreneau(datePicker.getValue(), heureSelectionnee, selectedRdv.getId());
        afficherRecommandation(recommandation);

        if (!recommandation.isEstValide() || !recommandation.isEstDisponible()) {
            showAlert(formatRecommendationMessage(recommandation));
            return;
        }

        selectedRdv.setDateRdv(datePicker.getValue());
        selectedRdv.setHeureRdv(heureSelectionnee);
        selectedRdv.setRemarque(remarqueArea.getText().trim());
        selectedRdv.setTypeRendezVous(comboType.getValue());

        rendezVousService.modifier(selectedRdv);
        showSuccess("Rendez-vous modifie avec succes.");
        clearFields();
        loadData();
    }

    @FXML
    public void clearFields() {
        datePicker.setValue(null);
        heureSpinner.getValueFactory().setValue(9);
        minuteSpinner.getValueFactory().setValue(0);
        statutField.clear();
        remarqueArea.clear();
        comboType.setValue(null);
        selectedRdv = null;
        rendezVousTable.getSelectionModel().clearSelection();
        afficherRecommandation();
    }

    @FXML
    public void resetFilters() {
        searchField.clear();
        statutFilter.setValue("Tous");
        typeFilter.setValue("Tous");
        sortCombo.setValue("Date croissante");
        refreshFilters();
        applySort();
    }

    private void configureTimeControls() {
        heureSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));
        heureSpinner.setEditable(true);
        minuteSpinner.setEditable(true);
    }

    private LocalTime buildSelectedTime() {
        return LocalTime.of(heureSpinner.getValue(), minuteSpinner.getValue());
    }

    private void configureColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateRdv"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureRdv"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colRemarque.setCellValueFactory(new PropertyValueFactory<>("remarque"));
        colType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeRendezVous().getLibelle())
        );
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("badge-cell", "badge-warning", "badge-success", "badge-danger");
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    return;
                }
                setText(item);
                getStyleClass().add("badge-cell");
                if (RendezVous.STATUT_ACCEPTE.equals(item)) {
                    getStyleClass().add("badge-success");
                } else if (RendezVous.STATUT_REFUSE.equals(item)) {
                    getStyleClass().add("badge-danger");
                } else {
                    getStyleClass().add("badge-warning");
                }
            }
        });
        configureActionsColumn();
    }

    private void configureActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button accepterButton = new Button("Accepter");
            private final Button refuserButton = new Button("Refuser");
            private final HBox actionsBox = new HBox(0, accepterButton, refuserButton);

            {
                accepterButton.getStyleClass().add("primary-button");
                refuserButton.getStyleClass().add("danger-button");
                accepterButton.setOnAction(event -> traiterStatut(RendezVous.STATUT_ACCEPTE));
                refuserButton.setOnAction(event -> traiterStatut(RendezVous.STATUT_REFUSE));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= rendezVousTable.getItems().size()) {
                    setGraphic(null);
                    return;
                }

                RendezVous rendezVous = rendezVousTable.getItems().get(getIndex());
                setGraphic(RendezVous.STATUT_EN_COURS.equals(rendezVous.getStatut()) ? actionsBox : null);
            }

            private void traiterStatut(String statut) {
                RendezVous rendezVous = rendezVousTable.getItems().get(getIndex());
                boolean updated = RendezVous.STATUT_ACCEPTE.equals(statut)
                        ? rendezVousService.accepter(rendezVous.getId())
                        : rendezVousService.refuser(rendezVous.getId());

                if (updated) {
                    EmailService.EmailResult emailResult = envoyerEmailStatut(rendezVous, statut);
                    if (emailResult.sent()) {
                        showSuccess("Statut mis a jour : " + statut + ".\nEmail envoye a l'utilisateur.");
                    } else {
                        showWarning("Statut mis a jour : " + statut + ".\nEmail non envoye : " + emailResult.message());
                    }
                    if (selectedRdv != null && selectedRdv.getId() == rendezVous.getId()) {
                        statutField.setText(statut);
                    }
                } else {
                    showAlert("Le rendez-vous a deja ete traite ou la mise a jour a echoue.");
                }
                loadData();
            }
        });
    }

    private EmailService.EmailResult envoyerEmailStatut(RendezVous rendezVous, String statut) {
        String email = rendezVousService.trouverEmailUtilisateur(rendezVous.getId()).orElse(null);
        return emailService.envoyerNotificationStatut(email, rendezVous, statut);
    }

    private void configureFilters() {
        statutFilter.getItems().setAll("Tous");
        typeFilter.getItems().setAll("Tous");
        sortCombo.getItems().setAll(
                "Date croissante",
                "Date decroissante",
                "Heure croissante",
                "Heure decroissante",
                "Type A-Z",
                "Type Z-A"
        );
        statutFilter.setValue("Tous");
        typeFilter.setValue("Tous");
        sortCombo.setValue("Date croissante");
    }

    private void installListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshFilters());
        statutFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshFilters());
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshFilters());
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySort());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> afficherRecommandation());
        heureSpinner.valueProperty().addListener((obs, oldVal, newVal) -> afficherRecommandation());
        minuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> afficherRecommandation());
    }

    private void refreshFilters() {
        String search = searchField.getText();
        String statut = statutFilter.getValue();
        String type = typeFilter.getValue();

        tableSupport.apply(rendezVous ->
                matchesSearch(rendezVous, search)
                        && matchesStatut(rendezVous, statut)
                        && matchesType(rendezVous, type)
        );
    }

    private boolean matchesSearch(RendezVous rendezVous, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String searchable = String.join(" ",
                String.valueOf(rendezVous.getId()),
                String.valueOf(rendezVous.getDateRdv()),
                String.valueOf(rendezVous.getHeureRdv()),
                safe(rendezVous.getStatut()),
                safe(rendezVous.getRemarque()),
                safe(rendezVous.getTypeRendezVous().getLibelle())
        );
        return TableFilterSupport.containsNormalized(searchable, search);
    }

    private boolean matchesStatut(RendezVous rendezVous, String statut) {
        return statut == null || "Tous".equals(statut) || statut.equalsIgnoreCase(rendezVous.getStatut());
    }

    private boolean matchesType(RendezVous rendezVous, String type) {
        return type == null || "Tous".equals(type) || type.equalsIgnoreCase(rendezVous.getTypeRendezVous().getLibelle());
    }

    private void applySort() {
        Comparator<RendezVous> comparator = switch (sortCombo.getValue()) {
            case "Date decroissante" -> Comparator.comparing(RendezVous::getDateRdv).reversed();
            case "Heure croissante" -> Comparator.comparing(RendezVous::getHeureRdv);
            case "Heure decroissante" -> Comparator.comparing(RendezVous::getHeureRdv).reversed();
            case "Type A-Z" -> Comparator.comparing(r -> r.getTypeRendezVous().getLibelle(), String.CASE_INSENSITIVE_ORDER);
            case "Type Z-A" -> Comparator.comparing((RendezVous r) -> r.getTypeRendezVous().getLibelle(), String.CASE_INSENSITIVE_ORDER).reversed();
            default -> Comparator.comparing(RendezVous::getDateRdv).thenComparing(RendezVous::getHeureRdv);
        };
        tableSupport.sortWith(comparator);
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

    private void afficherRecommandation() {
        if (datePicker.getValue() == null || heureSpinner.getValue() == null || minuteSpinner.getValue() == null) {
            recommandationLabel.setText("Selectionnez un rendez-vous ou choisissez une date et une heure.");
            updateRecommendationStyle("neutre");
            creneauxDisponiblesLabel.setText("Creneaux autorises : 09:00 a 14:30, toutes les 30 minutes.");
            return;
        }

        int rendezVousIdAIgnorer = selectedRdv == null ? 0 : selectedRdv.getId();
        afficherRecommandation(serviceRecommandation.analyserCreneau(
                datePicker.getValue(),
                buildSelectedTime(),
                rendezVousIdAIgnorer
        ));
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

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
