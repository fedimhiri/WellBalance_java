package service;

import model.PlanNutrition;
import utils.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanNutritionService implements IService<PlanNutrition> {

    private Connection connection;

    public PlanNutritionService() {
        connection = DataBaseConnection.getConnection();
    }

    @Override
    public void ajouter(PlanNutrition planNutrition) throws SQLException {
        String req = "INSERT INTO plan_nutrition (objectif, description, periode, date_debut, date_fin, user_id, nutritionniste_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pre = connection.prepareStatement(req);
        pre.setString(1, planNutrition.getObjectif());
        pre.setString(2, planNutrition.getDescription());
        pre.setString(3, planNutrition.getPeriode());
        pre.setTimestamp(4, planNutrition.getDateDebut());
        pre.setTimestamp(5, planNutrition.getDateFin());
        pre.setInt(6, planNutrition.getUserId());
        pre.setInt(7, planNutrition.getNutritionnisteId());
        pre.executeUpdate();
    }

    @Override
    public void modifier(PlanNutrition planNutrition) throws SQLException {
        String req = "UPDATE plan_nutrition SET objectif = ?, description = ?, periode = ?, date_debut = ?, date_fin = ?, user_id = ?, nutritionniste_id = ? WHERE id = ?";
        PreparedStatement pre = connection.prepareStatement(req);
        pre.setString(1, planNutrition.getObjectif());
        pre.setString(2, planNutrition.getDescription());
        pre.setString(3, planNutrition.getPeriode());
        pre.setTimestamp(4, planNutrition.getDateDebut());
        pre.setTimestamp(5, planNutrition.getDateFin());
        pre.setInt(6, planNutrition.getUserId());
        pre.setInt(7, planNutrition.getNutritionnisteId());
        pre.setInt(8, planNutrition.getId());
        pre.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Supprimer d'abord les enfants pour éviter l'erreur de clé étrangère
        String deleteChildReq = "DELETE FROM repas WHERE plan_nutrition_id = ?";
        PreparedStatement preChild = connection.prepareStatement(deleteChildReq);
        preChild.setInt(1, id);
        preChild.executeUpdate();

        // Ensuite, supprimer le parent
        String req = "DELETE FROM plan_nutrition WHERE id = ?";
        PreparedStatement pre = connection.prepareStatement(req);
        pre.setInt(1, id);
        pre.executeUpdate();
    }

    @Override
    public List<PlanNutrition> afficher() throws SQLException {
        List<PlanNutrition> list = new ArrayList<>();
        String req = "SELECT * FROM plan_nutrition ORDER BY id ASC";
        Statement ste = connection.createStatement();
        ResultSet res = ste.executeQuery(req);
        while (res.next()) {
            PlanNutrition planNutrition = new PlanNutrition();
            planNutrition.setId(res.getInt("id"));
            planNutrition.setObjectif(res.getString("objectif"));
            planNutrition.setDescription(res.getString("description"));
            planNutrition.setPeriode(res.getString("periode"));
            planNutrition.setDateDebut(res.getTimestamp("date_debut"));
            planNutrition.setDateFin(res.getTimestamp("date_fin"));
            planNutrition.setUserId(res.getInt("user_id"));
            planNutrition.setNutritionnisteId(res.getInt("nutritionniste_id"));
            list.add(planNutrition);
        }
        return list;
    }
}
