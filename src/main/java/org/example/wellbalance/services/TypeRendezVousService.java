package org.example.wellbalance.services;

import org.example.wellbalance.models.TypeRendezVous;
import org.example.wellbalance.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TypeRendezVousService {

    private final Connection cnx;

    public TypeRendezVousService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    public boolean ajouter(TypeRendezVous t) {
        if (cnx == null) {
            System.out.println("Connexion null.");
            return false;
        }

        String sql = "INSERT INTO type_rendezvous (libelle, description, duree, prix, categorie) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, t.getLibelle());
            ps.setString(2, t.getDescription());
            ps.setInt(3, t.getDuree());
            ps.setDouble(4, t.getPrix());
            ps.setString(5, t.getCategorie());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Erreur ajout TypeRendezVous : " + e.getMessage());
            return false;
        }
    }

    public List<TypeRendezVous> afficher() {
        List<TypeRendezVous> list = new ArrayList<>();

        if (cnx == null) {
            System.out.println("Connexion null.");
            return list;
        }

        String sql = "SELECT * FROM type_rendezvous";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TypeRendezVous t = new TypeRendezVous();
                t.setId(rs.getInt("id"));
                t.setLibelle(rs.getString("libelle"));
                t.setDescription(rs.getString("description"));
                t.setDuree(rs.getInt("duree"));
                t.setPrix(rs.getDouble("prix"));
                t.setCategorie(rs.getString("categorie"));

                list.add(t);
            }

        } catch (SQLException e) {
            System.out.println("Erreur affichage TypeRendezVous : " + e.getMessage());
        }

        return list;
    }

    public boolean modifier(TypeRendezVous t) {
        if (cnx == null) {
            System.out.println("Connexion null.");
            return false;
        }

        String sql = "UPDATE type_rendezvous SET libelle=?, description=?, duree=?, prix=?, categorie=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, t.getLibelle());
            ps.setString(2, t.getDescription());
            ps.setInt(3, t.getDuree());
            ps.setDouble(4, t.getPrix());
            ps.setString(5, t.getCategorie());
            ps.setInt(6, t.getId());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Erreur modification TypeRendezVous : " + e.getMessage());
            return false;
        }
    }

    public boolean supprimer(int id) {
        if (cnx == null) {
            System.out.println("Connexion null.");
            return false;
        }

        String sql = "DELETE FROM type_rendezvous WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Erreur suppression TypeRendezVous : " + e.getMessage());
            return false;
        }

    }

}