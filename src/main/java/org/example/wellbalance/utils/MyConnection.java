package org.example.wellbalance.utils;

import java.sql.*;

public class MyConnection {
    private static MyConnection instance;
    private Connection cnx;

    private final String url = "jdbc:mysql://localhost:3306/wellbalance1920";
    private final String user = "root";
    private final String pwd = "";

    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(url, user, pwd);
            System.out.println("Connexion réussie");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}