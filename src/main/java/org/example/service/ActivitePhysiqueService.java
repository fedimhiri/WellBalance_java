package org.example.service;

import org.example.config.Database;
import org.example.entity.ActivitePhysique;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivitePhysiqueService {
    private Connection connection;

    public ActivitePhysiqueService() {
        this.connection = Database.getInstance().getConnection();
    }

    public void create(ActivitePhysique obj) throws SQLException {
        String query = "INSERT INTO activite_physique (nom, description, type_activite, niveau, duree_estimee, calories_estimees, actif, objectif_sportif_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, obj.getNom());
            pst.setString(2, obj.getDescription());
            pst.setString(3, obj.getTypeActivite());
            pst.setString(4, obj.getNiveau());
            pst.setInt(5, obj.getDureeEstimee());
            pst.setInt(6, obj.getCaloriesEstimees());
            pst.setBoolean(7, obj.isActif());
            pst.setInt(8, obj.getObjectifSportifId());
            pst.executeUpdate();
            System.out.println("Activité Physique créée avec succès !");
        }
    }

    public List<ActivitePhysique> getAll() throws SQLException {
        List<ActivitePhysique> list = new ArrayList<>();
        String query = "SELECT * FROM activite_physique";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ActivitePhysique obj = new ActivitePhysique(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("type_activite"),
                        rs.getString("niveau"),
                        rs.getInt("duree_estimee"),
                        rs.getInt("calories_estimees"),
                        rs.getBoolean("actif"),
                        rs.getInt("objectif_sportif_id")
                );
                list.add(obj);
            }
        }
        return list;
    }

    public void update(ActivitePhysique obj) throws SQLException {
        String query = "UPDATE activite_physique SET nom=?, description=?, type_activite=?, niveau=?, duree_estimee=?, calories_estimees=?, actif=?, objectif_sportif_id=? WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, obj.getNom());
            pst.setString(2, obj.getDescription());
            pst.setString(3, obj.getTypeActivite());
            pst.setString(4, obj.getNiveau());
            pst.setInt(5, obj.getDureeEstimee());
            pst.setInt(6, obj.getCaloriesEstimees());
            pst.setBoolean(7, obj.isActif());
            pst.setInt(8, obj.getObjectifSportifId());
            pst.setInt(9, obj.getId());
            pst.executeUpdate();
            System.out.println("Activité Physique mise à jour avec succès !");
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM activite_physique WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Activité Physique supprimée avec succès !");
        }
    }
}
