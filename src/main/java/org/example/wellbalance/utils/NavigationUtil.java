package org.example.wellbalance.utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public final class NavigationUtil {

    private NavigationUtil() {
    }

    public static void openMenu(ActionEvent event) throws IOException {
        replaceScene(event, "/views/menu.fxml", "WellBalance - Dashboard", 1320, 820);
    }

    public static void navigate(ActionEvent event, String fxmlPath, String title) throws IOException {
        replaceScene(event, fxmlPath, title, 1320, 820);
    }

    public static void openModal(String fxmlPath, String title) throws IOException {
        URL fxmlUrl = NavigationUtil.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML introuvable : " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load(), 1320, 820);
        applyCss(scene);

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    private static void replaceScene(ActionEvent event, String fxmlPath, String title, double width, double height) throws IOException {
        URL fxmlUrl = NavigationUtil.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML introuvable : " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load(), width, height);
        applyCss(scene);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    private static void applyCss(Scene scene) {
        URL cssUrl = NavigationUtil.class.getResource("/css/modern-style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
    }
}
