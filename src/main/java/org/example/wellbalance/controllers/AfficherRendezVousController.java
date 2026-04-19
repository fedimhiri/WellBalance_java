package org.example.wellbalance.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.wellbalance.models.RendezVous;
import org.example.wellbalance.services.RendezVousService;
import org.example.wellbalance.utils.TableFilterSupport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;

public class AfficherRendezVousController extends BaseAdminController {

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
    private TextField searchField;
    @FXML
    private ComboBox<String> statutFilter;
    @FXML
    private ComboBox<String> typeFilter;
    @FXML
    private ComboBox<String> sortCombo;

    private final RendezVousService rendezVousService = new RendezVousService();
    private TableFilterSupport<RendezVous> tableSupport;

    @FXML
    public void initialize() {
        tableSupport = new TableFilterSupport<>(rendezVousTable);
        configureColumns();
        configureFilters();
        installListeners();
        loadData();
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
    public void resetFilters() {
        searchField.clear();
        statutFilter.setValue("Tous");
        typeFilter.setValue("Tous");
        sortCombo.setValue("Date croissante");
        refreshFilters();
        applySort();
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

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
