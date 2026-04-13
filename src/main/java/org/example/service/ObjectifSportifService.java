package org.example.service;

import org.example.config.Database;
import org.example.entity.ObjectifSportif;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ObjectifSportifService {
    private Connection connection;

    public ObjectifSportifService() {
        this.connection = Database.getInstance().getConnection();
    }

    public void create(ObjectifSportif obj) throws SQLException {
        String query = "INSERT INTO objectif_sportif (libelle, description, type_objectif, date_debut, date_fin, statut, weight, height, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, obj.getLibelle());
            pst.setString(2, obj.getDescription());
            pst.setString(3, obj.getTypeObjectif());
            pst.setDate(4, Date.valueOf(obj.getDateDebut()));
            pst.setDate(5, obj.getDateFin() != null ? Date.valueOf(obj.getDateFin()) : null);
            pst.setString(6, obj.getStatut());
            pst.setObject(7, obj.getWeight(), Types.DOUBLE);
            pst.setObject(8, obj.getHeight(), Types.DOUBLE);
            pst.setObject(9, obj.getUserId(), Types.INTEGER);
            pst.executeUpdate();
            System.out.println("Objectif Sportif créé avec succès !");
        }
    }

    public List<ObjectifSportif> getAll() throws SQLException {
        List<ObjectifSportif> list = new ArrayList<>();
        String query = "SELECT * FROM objectif_sportif";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObjectifSportif obj = new ObjectifSportif(
                        rs.getInt("id"),
                        rs.getString("libelle"),
                        rs.getString("description"),
                        rs.getString("type_objectif"),
                        rs.getDate("date_debut").toLocalDate(),
                        rs.getDate("date_fin") != null ? rs.getDate("date_fin").toLocalDate() : null,
                        rs.getString("statut"),
                        rs.getObject("weight", Double.class),
                        rs.getObject("height", Double.class),
                        rs.getObject("user_id", Integer.class)
                );
                list.add(obj);
            }
        }
        return list;
    }

    public void update(ObjectifSportif obj) throws SQLException {
        String query = "UPDATE objectif_sportif SET libelle=?, description=?, type_objectif=?, date_debut=?, date_fin=?, statut=?, weight=?, height=?, user_id=? WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, obj.getLibelle());
            pst.setString(2, obj.getDescription());
            pst.setString(3, obj.getTypeObjectif());
            pst.setDate(4, Date.valueOf(obj.getDateDebut()));
            pst.setDate(5, obj.getDateFin() != null ? Date.valueOf(obj.getDateFin()) : null);
            pst.setString(6, obj.getStatut());
            pst.setObject(7, obj.getWeight(), Types.DOUBLE);
            pst.setObject(8, obj.getHeight(), Types.DOUBLE);
            pst.setObject(9, obj.getUserId(), Types.INTEGER);
            pst.setInt(10, obj.getId());
            pst.executeUpdate();
            System.out.println("Objectif Sportif mis à jour avec succès !");
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM objectif_sportif WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Objectif Sportif supprimé avec succès !");
        }
    }
}
