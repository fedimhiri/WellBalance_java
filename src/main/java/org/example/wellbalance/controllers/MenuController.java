package org.example.wellbalance.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MenuController {

    private void openWindow(String fxmlPath, String title) throws IOException {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        System.out.println("FXML URL = " + fxmlUrl);

        if (fxmlUrl == null) {
            throw new IOException("FXML introuvable : " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load(), 1050, 650);

        URL cssUrl = getClass().getResource("/css/modern-style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void openAjouterType(ActionEvent event) throws IOException {
        openWindow("/views/ajouter-type-rdv.fxml", "Ajouter Type RendezVous");
    }

    @FXML
    public void openAfficherType(ActionEvent event) throws IOException {
        openWindow("/views/afficher-type-rdv.fxml", "Afficher Type RendezVous");
    }

    @FXML
    public void openModifierType(ActionEvent event) throws IOException {
        openWindow("/views/modifier-type-rdv.fxml", "Modifier Type RendezVous");
    }

    @FXML
    public void openSupprimerType(ActionEvent event) throws IOException {
        openWindow("/views/supprimer-type-rdv.fxml", "Supprimer Type RendezVous");
    }

    @FXML
    public void openAjouterRdv(ActionEvent event) throws IOException {
        openWindow("/views/ajouter-rdv.fxml", "Ajouter RendezVous");
    }

    @FXML
    public void openAfficherRdv(ActionEvent event) throws IOException {
        openWindow("/views/afficher-rdv.fxml", "Afficher RendezVous");
    }

    @FXML
    public void openModifierRdv(ActionEvent event) throws IOException {
        openWindow("/views/modifier-rdv.fxml", "Modifier RendezVous");
    }

    @FXML
    public void openSupprimerRdv(ActionEvent event) throws IOException {
        openWindow("/views/delete-rdv.fxml", "Supprimer RendezVous");
    }
}