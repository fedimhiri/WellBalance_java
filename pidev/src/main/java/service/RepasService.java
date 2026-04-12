package service;

import model.Repas;
import utils.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepasService implements IService<Repas> {

    private Connection connection;

    public RepasService() {
        connection = DataBaseConnection.getConnection();
    }

    @Override
    public void ajouter(Repas repas) throws SQLException {
        String req = "INSERT INTO repas (type_repas, calories, proteines, glucides, lipides, portion_size, barcode, description, date_repas, plan_nutrition_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pre = connection.prepareStatement(req);
        pre.setString(1, repas.getTypeRepas());
        pre.setFloat(2, repas.getCalories());
        pre.setFloat(3, repas.getProteines());
        pre.setFloat(4, repas.getGlucides());
        pre.setFloat(5, repas.getLipides());
        pre.setString(6, repas.getPortionSize());
        pre.setString(7, repas.getBarcode());
        pre.setString(8, repas.getDescription());
        pre.setTimestamp(9, repas.getDateRepas());
        pre.setInt(10, repas.getPlanNutritionId());
        pre.executeUpdate();
    }

    @Override
    public void modifier(Repas repas) throws SQLException {
        String req = "UPDATE repas SET type_repas = ?, calories = ?, proteines = ?, glucides = ?, lipides = ?, portion_size = ?, barcode = ?, description = ?, date_repas = ?, plan_nutrition_id = ? WHERE id = ?";
        PreparedStatement pre = connection.prepareStatement(req);
        pre.setString(1, repas.getTypeRepas());
        pre.setFloat(2, repas.getCalories());
        pre.setFloat(3, repas.getProteines());
        pre.setFloat(4, repas.getGlucides());
        pre.setFloat(5, repas.getLipides());
        pre.setString(6, repas.getPortionSize());
        pre.setString(7, repas.getBarcode());
        pre.setString(8, repas.getDescription());
        pre.setTimestamp(9, repas.getDateRepas());
        pre.setInt(10, repas.getPlanNutritionId());
        pre.setInt(11, repas.getId());
        pre.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM repas WHERE id = ?";
        PreparedStatement pre = connection.prepareStatement(req);
        pre.setInt(1, id);
        pre.executeUpdate();
    }

    @Override
    public List<Repas> afficher() throws SQLException {
        List<Repas> list = new ArrayList<>();
        String req = "SELECT * FROM repas ORDER BY id ASC";
        Statement ste = connection.createStatement();
        ResultSet res = ste.executeQuery(req);
        while (res.next()) {
            Repas repas = new Repas();
            repas.setId(res.getInt("id"));
            repas.setTypeRepas(res.getString("type_repas"));
            repas.setCalories(res.getFloat("calories"));
            repas.setProteines(res.getFloat("proteines"));
            repas.setGlucides(res.getFloat("glucides"));
            repas.setLipides(res.getFloat("lipides"));
            repas.setPortionSize(res.getString("portion_size"));
            repas.setBarcode(res.getString("barcode"));
            repas.setDescription(res.getString("description"));
            repas.setDateRepas(res.getTimestamp("date_repas"));
            repas.setPlanNutritionId(res.getInt("plan_nutrition_id"));
            list.add(repas);
        }
        return list;
    }
}
