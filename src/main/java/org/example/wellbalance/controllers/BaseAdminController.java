package org.example.wellbalance.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.example.wellbalance.utils.NavigationUtil;

import java.io.IOException;
import java.util.Optional;

public abstract class BaseAdminController {

    public void openDashboard(ActionEvent event) throws IOException {
        NavigationUtil.navigate(event, "/views/menu.fxml", "WellBalance - Dashboard");
    }

    public void openAjouterRdv(ActionEvent event) throws IOException {
        NavigationUtil.navigate(event, "/views/ajouter-rdv.fxml", "Ajouter RendezVous");
    }

    public void openAfficherRdv(ActionEvent event) throws IOException {
        NavigationUtil.navigate(event, "/views/afficher-rdv.fxml", "Afficher RendezVous");
    }

    public void openModifierRdv(ActionEvent event) throws IOException {
        NavigationUtil.navigate(event, "/views/modifier-rdv.fxml", "Modifier RendezVous");
    }

    public void openSupprimerRdv(ActionEvent event) throws IOException {
        NavigationUtil.navigate(event, "/views/delete-rdv.fxml", "Supprimer RendezVous");
    }

    public void openAjouterType(ActionEvent event) throws IOException {
        NavigationUtil.navigate(event, "/views/ajouter-type-rdv.fxml", "Ajouter Type RendezVous");
    }

    public void openAfficherType(ActionEvent event) throws IOException {
        NavigationUtil.navigate(event, "/views/afficher-type-rdv.fxml", "Afficher Type RendezVous");
    }

    public void openModifierType(ActionEvent event) throws IOException {
        NavigationUtil.navigate(event, "/views/modifier-type-rdv.fxml", "Modifier Type RendezVous");
    }

    public void openSupprimerType(ActionEvent event) throws IOException {
        NavigationUtil.navigate(event, "/views/supprimer-type-rdv.fxml", "Supprimer Type RendezVous");
    }

    public void retourMenu(ActionEvent event) throws IOException {
        openDashboard(event);
    }

    protected boolean confirmDeletion(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Voulez-vous vraiment supprimer cet élément ?");
        alert.setContentText(message);

        // On garde uniquement les boutons demandes : Oui et Annuler.
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
