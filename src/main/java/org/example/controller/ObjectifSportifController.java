package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.entity.ActivitePhysique;
import org.example.entity.ObjectifSportif;
import org.example.service.ActivitePhysiqueService;
import org.example.service.ObjectifSportifService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ObjectifSportifController {

    // --- Navigation ---
    @FXML private StackPane mainContent;
    @FXML private VBox pageObjectifs;
    @FXML private VBox pageActivites;
    @FXML private VBox pageFormObj;
    @FXML private VBox pageFormAct;
    @FXML private Button navObjectifs;
    @FXML private Button navActivites;
    @FXML private Button navFormObj;
    @FXML private Button navFormAct;
    @FXML private FlowPane gridObjectifs;
    @FXML private FlowPane gridActivites;
    @FXML private HBox statsRowObj;
    @FXML private HBox statsRowAct;
    @FXML private Label lblFormTitleObj;
    @FXML private Label lblFormTitleAct;
    @FXML private Button btnSubmitObj;
    @FXML private Button btnSubmitAct;

    // --- Objectif Form Fields ---
    @FXML private TextField txtLibelle;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbTypeObj;
    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextField txtWeight;
    @FXML private TextField txtHeight;

    // Error Labels Objectifs
    @FXML private Label lblErrorLibelle;
    @FXML private Label lblErrorDesc;
    @FXML private Label lblErrorType;
    @FXML private Label lblErrorDateDebut;
    @FXML private Label lblErrorDateFin;
    @FXML private Label lblErrorStatut;
    @FXML private Label lblErrorWeight;
    @FXML private Label lblErrorHeight;

    // --- Activité Form Fields ---
    @FXML private TextField txtNomAct;
    @FXML private TextArea txtDescAct;
    @FXML private ComboBox<String> cbTypeAct;
    @FXML private ComboBox<String> cbNiveauAct;
    @FXML private TextField txtDuree;
    @FXML private TextField txtCalories;
    @FXML private CheckBox chkActif;
    @FXML private ComboBox<ObjectifSportif> cbObjectif;

    // Error Labels Activités
    @FXML private Label lblErrorNomAct;
    @FXML private Label lblErrorDescAct;
    @FXML private Label lblErrorTypeAct;
    @FXML private Label lblErrorNiveauAct;
    @FXML private Label lblErrorDuree;
    @FXML private Label lblErrorCalories;
    @FXML private Label lblErrorObjectifAct;

    // Services
    private final ObjectifSportifService objService = new ObjectifSportifService();
    private final ActivitePhysiqueService actService = new ActivitePhysiqueService();

    // Data
    private final ObservableList<ObjectifSportif> objectiveList = FXCollections.observableArrayList();
    private final ObservableList<ActivitePhysique> activityList = FXCollections.observableArrayList();

    // Selection
    private ObjectifSportif selectedObjective = null;
    private ActivitePhysique selectedActivity = null;

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        setupComboBoxes();
        bindErrorLabels();
        refreshAllData();
    }

    private void bindErrorLabels() {
        // Bind managed to visible so error labels properly show/hide in layout
        Label[] errorLabels = {
            lblErrorLibelle, lblErrorDesc, lblErrorType, lblErrorDateDebut,
            lblErrorDateFin, lblErrorStatut, lblErrorWeight, lblErrorHeight,
            lblErrorNomAct, lblErrorDescAct, lblErrorTypeAct, lblErrorNiveauAct,
            lblErrorDuree, lblErrorCalories, lblErrorObjectifAct
        };
        for (Label lbl : errorLabels) {
            if (lbl != null) {
                lbl.managedProperty().bind(lbl.visibleProperty());
            }
        }
    }

    // ==========================================
    //  NAVIGATION
    // ==========================================

    private void showPage(VBox page) {
        pageObjectifs.setVisible(false);
        pageActivites.setVisible(false);
        pageFormObj.setVisible(false);
        pageFormAct.setVisible(false);
        page.setVisible(true);

        // Update sidebar active state
        navObjectifs.getStyleClass().setAll(page == pageObjectifs ? "sidebar-nav-item-active" : "sidebar-nav-item");
        navActivites.getStyleClass().setAll(page == pageActivites ? "sidebar-nav-item-active" : "sidebar-nav-item");
        navFormObj.getStyleClass().setAll(page == pageFormObj ? "sidebar-nav-item-active" : "sidebar-nav-item");
        navFormAct.getStyleClass().setAll(page == pageFormAct ? "sidebar-nav-item-active" : "sidebar-nav-item");
    }

    @FXML void showObjectifsPage() {
        refreshAllData();
        showPage(pageObjectifs);
    }

    @FXML void showActivitesPage() {
        refreshAllData();
        showPage(pageActivites);
    }

    @FXML void showAddFormObj() {
        selectedObjective = null;
        handleVider(null);
        lblFormTitleObj.setText("📝 Nouvel Objectif");
        btnSubmitObj.setText("Créer l'Objectif");
        showPage(pageFormObj);
    }

    @FXML void showAddFormAct() {
        selectedActivity = null;
        handleViderAct(null);
        lblFormTitleAct.setText("📝 Nouvelle Activité");
        btnSubmitAct.setText("Créer l'Activité");
        showPage(pageFormAct);
    }

    // ==========================================
    //  DATA LOADING
    // ==========================================

    private void refreshAllData() {
        try {
            objectiveList.setAll(objService.getAll());
            activityList.setAll(actService.getAll());
            cbObjectif.setItems(objectiveList);
            renderObjectifGrid();
            renderActivityGrid();
            renderStats();
        } catch (SQLException e) {
            showAlert("Erreur", "Base de données inaccessible : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ==========================================
    //  STATS RENDERING
    // ==========================================

    private void renderStats() {
        statsRowObj.getChildren().clear();
        long total = objectiveList.size();
        long enCours = objectiveList.stream().filter(o -> "En cours".equals(o.getStatut())).count();
        long termines = objectiveList.stream().filter(o -> "Terminé".equals(o.getStatut())).count();

        statsRowObj.getChildren().addAll(
            createStatCard("🎯", String.valueOf(total), "Total Objectifs", "#4E97D1"),
            createStatCard("⏳", String.valueOf(enCours), "En Cours", "#f39c12"),
            createStatCard("✅", String.valueOf(termines), "Terminés", "#27ae60")
        );

        statsRowAct.getChildren().clear();
        long totalAct = activityList.size();
        long actifs = activityList.stream().filter(ActivitePhysique::isActif).count();
        int totalCal = activityList.stream().mapToInt(ActivitePhysique::getCaloriesEstimees).sum();

        statsRowAct.getChildren().addAll(
            createStatCard("🏃", String.valueOf(totalAct), "Total Activités", "#4E97D1"),
            createStatCard("💪", String.valueOf(actifs), "Actives", "#27ae60"),
            createStatCard("🔥", String.valueOf(totalCal), "Calories Totales", "#e74c3c")
        );
    }

    private Node createStatCard(String icon, String number, String label, String color) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(180);
        HBox.setHgrow(card, Priority.ALWAYS);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label iconLbl = new Label(icon);
        iconLbl.getStyleClass().add("stat-icon");

        VBox info = new VBox(2);
        Label numLbl = new Label(number);
        numLbl.getStyleClass().add("stat-number");
        numLbl.setStyle("-fx-text-fill: " + color + ";");
        Label lblLbl = new Label(label);
        lblLbl.getStyleClass().add("stat-label");
        info.getChildren().addAll(numLbl, lblLbl);

        top.getChildren().addAll(iconLbl, info);
        card.getChildren().add(top);
        return card;
    }

    // ==========================================
    //  CARD RENDERING - OBJECTIFS
    // ==========================================

    private void renderObjectifGrid() {
        gridObjectifs.getChildren().clear();
        for (ObjectifSportif obj : objectiveList) {
            gridObjectifs.getChildren().add(createObjectifCard(obj));
        }
    }

    private Node createObjectifCard(ObjectifSportif obj) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        card.setPrefWidth(280);

        // --- Header with gradient ---
        HBox header = new HBox(8);
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(obj.getLibelle());
        titleLbl.getStyleClass().add("card-header-title");
        titleLbl.setMaxWidth(170);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        Label badge = new Label(obj.getStatut());
        if ("Terminé".equals(obj.getStatut())) {
            badge.getStyleClass().add("badge-termine");
        } else if ("Annulé".equals(obj.getStatut())) {
            badge.getStyleClass().add("badge-annule");
        } else {
            badge.getStyleClass().add("card-header-badge");
        }

        header.getChildren().addAll(titleLbl, badge);

        // --- Body with ALL details ---
        VBox body = new VBox(8);
        body.getStyleClass().add("card-body");

        // Description
        if (obj.getDescription() != null && !obj.getDescription().isEmpty()) {
            Label desc = new Label(obj.getDescription());
            desc.getStyleClass().add("card-desc");
            desc.setWrapText(true);
            desc.setMaxHeight(40);
            body.getChildren().add(desc);
        }

        // Type
        body.getChildren().add(createInfoRow("📂", "Type", obj.getTypeObjectif()));

        // Dates
        String dateStr = formatDate(obj.getDateDebut());
        if (obj.getDateFin() != null) {
            dateStr += "  →  " + formatDate(obj.getDateFin());
        }
        body.getChildren().add(createInfoRow("📅", "Période", dateStr));

        // Duration (jours restants)
        if (obj.getDateFin() != null && "En cours".equals(obj.getStatut())) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), obj.getDateFin());
            String daysStr = days > 0 ? days + " jours restants" : "Expiré";
            body.getChildren().add(createInfoRow("⏱", "Durée", daysStr));
        }

        // Poids & Taille
        HBox metrics = new HBox(15);
        metrics.setAlignment(Pos.CENTER_LEFT);
        if (obj.getWeight() != null) {
            metrics.getChildren().add(createInfoRow("⚖", "Poids", obj.getWeight() + " kg"));
        }
        if (obj.getHeight() != null) {
            metrics.getChildren().add(createInfoRow("📏", "Taille", obj.getHeight() + " cm"));
        }
        if (!metrics.getChildren().isEmpty()) {
            body.getChildren().add(metrics);
        }

        // --- Actions ---
        HBox actions = new HBox(8);
        actions.getStyleClass().add("card-actions");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEdit = new Button("✏ Modifier");
        btnEdit.getStyleClass().add("btn-card-edit");
        btnEdit.setOnAction(e -> {
            selectedObjective = obj;
            populateObjectifForm(obj);
            lblFormTitleObj.setText("✏ Modifier l'Objectif");
            btnSubmitObj.setText("Sauvegarder");
            showPage(pageFormObj);
        });

        Button btnDel = new Button("🗑 Supprimer");
        btnDel.getStyleClass().add("btn-card-delete");
        btnDel.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet objectif ?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    selectedObjective = obj;
                    handleSupprimer(null);
                }
            });
        });

        actions.getChildren().addAll(spacer, btnEdit, btnDel);

        card.getChildren().addAll(header, body, actions);
        return card;
    }

    // ==========================================
    //  CARD RENDERING - ACTIVITÉS
    // ==========================================

    private void renderActivityGrid() {
        gridActivites.getChildren().clear();
        for (ActivitePhysique act : activityList) {
            gridActivites.getChildren().add(createActivityCard(act));
        }
    }

    private Node createActivityCard(ActivitePhysique act) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        card.setPrefWidth(280);

        // Header
        HBox header = new HBox(8);
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(act.getNom());
        titleLbl.getStyleClass().add("card-header-title");
        titleLbl.setMaxWidth(170);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        Label badge = new Label(act.isActif() ? "Actif" : "Inactif");
        badge.getStyleClass().add(act.isActif() ? "badge-termine" : "badge-annule");

        header.getChildren().addAll(titleLbl, badge);

        // Body
        VBox body = new VBox(8);
        body.getStyleClass().add("card-body");

        // Description
        if (act.getDescription() != null && !act.getDescription().isEmpty()) {
            Label desc = new Label(act.getDescription());
            desc.getStyleClass().add("card-desc");
            desc.setWrapText(true);
            desc.setMaxHeight(40);
            body.getChildren().add(desc);
        }

        body.getChildren().add(createInfoRow("🏋", "Type", act.getTypeActivite()));
        body.getChildren().add(createInfoRow("📊", "Niveau", act.getNiveau()));

        // Metrics row
        HBox metrics = new HBox(15);
        metrics.setAlignment(Pos.CENTER_LEFT);
        metrics.getChildren().add(createInfoRow("⏱", "Durée", act.getDureeEstimee() + " min"));
        metrics.getChildren().add(createInfoRow("🔥", "Calories", act.getCaloriesEstimees() + " kcal"));
        body.getChildren().add(metrics);

        // Linked objective
        String objName = objectiveList.stream()
                .filter(o -> o.getId() == act.getObjectifSportifId())
                .map(ObjectifSportif::getLibelle)
                .findFirst().orElse("—");
        body.getChildren().add(createInfoRow("🎯", "Objectif", objName));

        // Actions
        HBox actions = new HBox(8);
        actions.getStyleClass().add("card-actions");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEdit = new Button("✏ Modifier");
        btnEdit.getStyleClass().add("btn-card-edit");
        btnEdit.setOnAction(e -> {
            selectedActivity = act;
            populateActivityForm(act);
            lblFormTitleAct.setText("✏ Modifier l'Activité");
            btnSubmitAct.setText("Sauvegarder");
            showPage(pageFormAct);
        });

        Button btnDel = new Button("🗑 Supprimer");
        btnDel.getStyleClass().add("btn-card-delete");
        btnDel.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette activité ?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    selectedActivity = act;
                    handleSupprimerAct(null);
                }
            });
        });

        actions.getChildren().addAll(spacer, btnEdit, btnDel);
        card.getChildren().addAll(header, body, actions);
        return card;
    }

    // ==========================================
    //  HELPERS
    // ==========================================

    private HBox createInfoRow(String icon, String label, String value) {
        HBox row = new HBox(4);
        row.getStyleClass().add("card-info-row");
        Label iconLbl = new Label(icon);
        iconLbl.getStyleClass().add("card-info-icon");
        Label valueLbl = new Label(value != null ? value : "—");
        valueLbl.getStyleClass().add("card-info-value");
        row.getChildren().addAll(iconLbl, valueLbl);
        return row;
    }

    private String formatDate(LocalDate d) {
        return d != null ? d.format(dateFmt) : "—";
    }

    // ==========================================
    //  VALIDATION
    // ==========================================

    private boolean validateObjectifForm() {
        clearErrors();
        boolean ok = true;

        if (txtLibelle.getText().trim().length() <= 5) {
            lblErrorLibelle.setText("Minimum 6 caractères"); lblErrorLibelle.setVisible(true); ok = false;
        }
        if (txtDescription.getText().trim().length() <= 5) {
            lblErrorDesc.setText("Minimum 6 caractères"); lblErrorDesc.setVisible(true); ok = false;
        }
        if (cbTypeObj.getValue() == null) {
            lblErrorType.setText("Type requis"); lblErrorType.setVisible(true); ok = false;
        }

        LocalDate debut = dpDebut.getValue();
        if (debut == null) {
            lblErrorDateDebut.setText("Date début requise"); lblErrorDateDebut.setVisible(true); ok = false;
        } else if (selectedObjective == null && debut.isBefore(LocalDate.now())) {
            lblErrorDateDebut.setText("Date ≥ aujourd'hui"); lblErrorDateDebut.setVisible(true); ok = false;
        }

        LocalDate fin = dpFin.getValue();
        if (debut != null && fin != null && fin.isBefore(debut)) {
            lblErrorDateFin.setText("Date fin ≥ début"); lblErrorDateFin.setVisible(true); ok = false;
        }

        if (txtWeight.getText().trim().isEmpty()) {
            lblErrorWeight.setText("Poids requis"); lblErrorWeight.setVisible(true); ok = false;
        } else {
            try { if (Double.parseDouble(txtWeight.getText()) <= 0) throw new Exception(); }
            catch (Exception e) { lblErrorWeight.setText("Nombre positif"); lblErrorWeight.setVisible(true); ok = false; }
        }

        if (txtHeight.getText().trim().isEmpty()) {
            lblErrorHeight.setText("Taille requise"); lblErrorHeight.setVisible(true); ok = false;
        } else {
            try { if (Double.parseDouble(txtHeight.getText()) <= 0) throw new Exception(); }
            catch (Exception e) { lblErrorHeight.setText("Nombre positif"); lblErrorHeight.setVisible(true); ok = false; }
        }
        return ok;
    }

    private boolean validateActivityForm() {
        clearErrors();
        boolean ok = true;

        if (txtNomAct.getText().trim().length() < 4) {
            lblErrorNomAct.setText("Minimum 4 caractères"); lblErrorNomAct.setVisible(true); ok = false;
        }
        if (txtDescAct.getText().trim().length() <= 5) {
            lblErrorDescAct.setText("Minimum 6 caractères"); lblErrorDescAct.setVisible(true); ok = false;
        }
        if (cbTypeAct.getValue() == null) {
            lblErrorTypeAct.setText("Type requis"); lblErrorTypeAct.setVisible(true); ok = false;
        }
        if (cbNiveauAct.getValue() == null) {
            lblErrorNiveauAct.setText("Niveau requis"); lblErrorNiveauAct.setVisible(true); ok = false;
        }
        if (txtDuree.getText().isEmpty()) {
            lblErrorDuree.setText("Durée requise"); lblErrorDuree.setVisible(true); ok = false;
        } else {
            try { if (Integer.parseInt(txtDuree.getText()) <= 0) throw new Exception(); }
            catch (Exception e) { lblErrorDuree.setText("Entier positif"); lblErrorDuree.setVisible(true); ok = false; }
        }
        if (txtCalories.getText().isEmpty()) {
            lblErrorCalories.setText("Calories requises"); lblErrorCalories.setVisible(true); ok = false;
        } else {
            try { if (Integer.parseInt(txtCalories.getText()) <= 0) throw new Exception(); }
            catch (Exception e) { lblErrorCalories.setText("Entier positif"); lblErrorCalories.setVisible(true); ok = false; }
        }
        if (cbObjectif.getValue() == null) {
            lblErrorObjectifAct.setText("Objectif requis"); lblErrorObjectifAct.setVisible(true); ok = false;
        }
        return ok;
    }

    private void clearErrors() {
        lblErrorLibelle.setVisible(false); lblErrorDesc.setVisible(false); lblErrorType.setVisible(false);
        lblErrorDateDebut.setVisible(false); lblErrorDateFin.setVisible(false);
        lblErrorStatut.setVisible(false); lblErrorWeight.setVisible(false); lblErrorHeight.setVisible(false);
        lblErrorNomAct.setVisible(false); lblErrorDescAct.setVisible(false); lblErrorTypeAct.setVisible(false);
        lblErrorNiveauAct.setVisible(false); lblErrorDuree.setVisible(false); lblErrorCalories.setVisible(false);
        lblErrorObjectifAct.setVisible(false);
    }

    // ==========================================
    //  CRUD HANDLERS
    // ==========================================

    @FXML void handleAjouter(ActionEvent event) {
        if (!validateObjectifForm()) return;
        try {
            ObjectifSportif obj = getObjectifFromForm();
            if (selectedObjective != null) {
                obj.setId(selectedObjective.getId());
                objService.update(obj);
            } else {
                objService.create(obj);
            }
            showObjectifsPage();
            showAlert("Succès", "Objectif sauvegardé avec succès.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "Échec : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML void handleSupprimer(ActionEvent event) {
        if (selectedObjective == null) return;
        try {
            objService.delete(selectedObjective.getId());
            selectedObjective = null;
            refreshAllData();
            showAlert("Succès", "Objectif supprimé.", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Suppression impossible (activités liées).", Alert.AlertType.ERROR);
        }
    }

    @FXML void handleVider(ActionEvent event) {
        txtLibelle.clear(); txtDescription.clear(); cbTypeObj.setValue(null);
        dpDebut.setValue(null); dpFin.setValue(null); cbStatut.setValue("En cours");
        txtWeight.clear(); txtHeight.clear(); clearErrors();
    }

    @FXML void handleAjouterAct(ActionEvent event) {
        if (!validateActivityForm()) return;
        try {
            ActivitePhysique act = getActivityFromForm();
            if (selectedActivity != null) {
                act.setId(selectedActivity.getId());
                actService.update(act);
            } else {
                actService.create(act);
            }
            showActivitesPage();
            showAlert("Succès", "Activité sauvegardée avec succès.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "Données invalides : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML void handleSupprimerAct(ActionEvent event) {
        if (selectedActivity == null) return;
        try {
            actService.delete(selectedActivity.getId());
            selectedActivity = null;
            refreshAllData();
            showAlert("Succès", "Activité supprimée.", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Échec : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML void handleViderAct(ActionEvent event) {
        txtNomAct.clear(); txtDescAct.clear(); cbTypeAct.setValue(null);
        cbNiveauAct.setValue(null); txtDuree.clear(); txtCalories.clear();
        chkActif.setSelected(true); cbObjectif.setValue(null); clearErrors();
    }

    // ==========================================
    //  FORM HELPERS
    // ==========================================

    private void setupComboBoxes() {
        cbStatut.getItems().addAll("En cours", "Terminé", "Annulé");
        cbStatut.setValue("En cours");
        cbTypeObj.getItems().addAll("Perte de poids", "Prise de masse", "Endurance", "Flexibilité", "Santé générale");
        cbTypeAct.getItems().addAll("Cardio", "Musculation", "Yoga", "Natation", "Cyclisme", "Running");
        cbNiveauAct.getItems().addAll("Débutant", "Intermédiaire", "Avancé");

        cbObjectif.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(ObjectifSportif item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getLibelle());
            }
        });
        cbObjectif.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(ObjectifSportif item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getLibelle());
            }
        });
    }

    private ObjectifSportif getObjectifFromForm() {
        return new ObjectifSportif(txtLibelle.getText(), txtDescription.getText(), cbTypeObj.getValue(),
                dpDebut.getValue(), dpFin.getValue(), cbStatut.getValue(),
                Double.parseDouble(txtWeight.getText().trim()),
                Double.parseDouble(txtHeight.getText().trim()), 1);
    }

    private ActivitePhysique getActivityFromForm() {
        return new ActivitePhysique(txtNomAct.getText(), txtDescAct.getText(), cbTypeAct.getValue(),
                cbNiveauAct.getValue(), Integer.parseInt(txtDuree.getText()),
                Integer.parseInt(txtCalories.getText()), chkActif.isSelected(),
                cbObjectif.getValue().getId());
    }

    private void populateObjectifForm(ObjectifSportif obj) {
        txtLibelle.setText(obj.getLibelle()); txtDescription.setText(obj.getDescription());
        cbTypeObj.setValue(obj.getTypeObjectif()); dpDebut.setValue(obj.getDateDebut());
        dpFin.setValue(obj.getDateFin()); cbStatut.setValue(obj.getStatut());
        txtWeight.setText(obj.getWeight() != null ? String.valueOf(obj.getWeight()) : "");
        txtHeight.setText(obj.getHeight() != null ? String.valueOf(obj.getHeight()) : "");
        clearErrors();
    }

    private void populateActivityForm(ActivitePhysique act) {
        txtNomAct.setText(act.getNom()); txtDescAct.setText(act.getDescription());
        cbTypeAct.setValue(act.getTypeActivite()); cbNiveauAct.setValue(act.getNiveau());
        txtDuree.setText(String.valueOf(act.getDureeEstimee()));
        txtCalories.setText(String.valueOf(act.getCaloriesEstimees()));
        chkActif.setSelected(act.isActif());
        objectiveList.stream().filter(o -> o.getId() == act.getObjectifSportifId()).findFirst().ifPresent(cbObjectif::setValue);
        clearErrors();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
