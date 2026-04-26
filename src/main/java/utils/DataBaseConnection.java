// File: src/main/java/utils/DatabaseConnection.java
package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {

    // === Database credentials ===
    private static final String URL = "jdbc:mysql://localhost:3306/wellbalance"; // <-- your database name
    private static final String USER = "root";       // <-- change if needed
    private static final String PASSWORD = "";   // <-- change if needed


    private static Connection connection = null;

    // === Method to get the connection ===
    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connected to the database successfully!");
            } catch (SQLException e) {
                System.err.println("Failed to connect to the database.");
                e.printStackTrace();
            }
        }
        return connection;
    }

    // === Close the connection ===
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }

    // === Test the connection ===
    public static void main(String[] args) {
        Connection conn = DataBaseConnection.getConnection();

        if (conn != null) {
            System.out.println("Database is ready to use!");
        } else {
            System.out.println("Connection failed.");
        }

        // Close the connection when done
        DataBaseConnection.closeConnection();
    }
}