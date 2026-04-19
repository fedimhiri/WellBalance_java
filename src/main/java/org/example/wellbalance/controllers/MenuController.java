package org.example.wellbalance.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.example.wellbalance.models.RendezVous;
import org.example.wellbalance.services.RendezVousService;

public class MenuController extends BaseAdminController {

    @FXML
    private Label totalRdvLabel;
    @FXML
    private Label pendingRdvLabel;
    @FXML
    private Label acceptedRdvLabel;
    @FXML
    private Label refusedRdvLabel;
    @FXML
    private Label todayRdvLabel;
    @FXML
    private PieChart statutChart;
    @FXML
    private BarChart<String, Number> activiteChart;

    private final RendezVousService rendezVousService = new RendezVousService();

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    @FXML
    public void refreshDashboard() {
        int total = rendezVousService.compterTous();
        int enCours = rendezVousService.compterParStatut(RendezVous.STATUT_EN_COURS);
        int acceptes = rendezVousService.compterParStatut(RendezVous.STATUT_ACCEPTE);
        int refuses = rendezVousService.compterParStatut(RendezVous.STATUT_REFUSE);
        int aujourdhui = rendezVousService.compterAujourdhui();

        totalRdvLabel.setText(String.valueOf(total));
        pendingRdvLabel.setText(String.valueOf(enCours));
        acceptedRdvLabel.setText(String.valueOf(acceptes));
        refusedRdvLabel.setText(String.valueOf(refuses));
        todayRdvLabel.setText(String.valueOf(aujourdhui));

        statutChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("En cours", enCours),
                new PieChart.Data("Acceptes", acceptes),
                new PieChart.Data("Refuses", refuses)
        ));
        statutChart.setLabelsVisible(true);
        statutChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Total", total));
        series.getData().add(new XYChart.Data<>("En cours", enCours));
        series.getData().add(new XYChart.Data<>("Acceptes", acceptes));
        series.getData().add(new XYChart.Data<>("Refuses", refuses));
        series.getData().add(new XYChart.Data<>("Aujourd'hui", aujourdhui));
        activiteChart.getData().setAll(series);
    }

}
