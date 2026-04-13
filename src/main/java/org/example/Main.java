package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.config.Database;
import java.sql.Connection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Test de la connexion avant de lancer l'UI
        Database db = Database.getInstance();
        Connection conn = db.getConnection();

        if (conn == null) {
            System.err.println("La connexion a échoué. Arrêt de l'application.");
            System.exit(1);
        }

        Parent root = FXMLLoader.load(getClass().getResource("/org/example/objectif_sportif.fxml"));
        primaryStage.setTitle("WellBalance - Sport Dashboard");
        Scene scene = new Scene(root, 1100, 750);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}