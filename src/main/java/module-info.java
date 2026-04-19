module org.example.wellbalance {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jakarta.mail;

    opens org.example.wellbalance to javafx.fxml;
    opens org.example.wellbalance.controllers to javafx.fxml;
    opens org.example.wellbalance.models to javafx.base, javafx.fxml;

    exports org.example.wellbalance;
    exports org.example.wellbalance.controllers;
    exports org.example.wellbalance.models;
    exports org.example.wellbalance.services;
    exports org.example.wellbalance.utils;
}
