package tn.esprit.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    //DB properties
    final String URL = "jdbc:mysql://localhost:3307/wellbalance";
    final String USR = "root";
    final String PWD = "";

    //Attributes
    //2. static instance
    static MyConnection instance = null;
    Connection cnx;

    public static MyConnection getInstance() {
        //3 verif
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }

    //constructor
    //1 : Privatisation du constructeur
    private MyConnection(){
        try {
            cnx = DriverManager.getConnection(URL, USR, PWD);
            System.out.println("Connexion etablie avec succes!");
        } catch (SQLException e) {
            System.err.println("===============================================");
            System.err.println("ERREUR: Impossible de se connecter a MySQL!");
            System.err.println("===============================================");
            System.err.println("Verifiez que:");
            System.err.println("1. XAMPP/WAMP est demarre");
            System.err.println("2. MySQL est actif (bouton 'Start' dans XAMPP)");
            System.err.println("3. La base 'wellbalance' existe");
            System.err.println("===============================================");
            System.err.println("Details techniques:");
            e.printStackTrace();
        }
    }





}
