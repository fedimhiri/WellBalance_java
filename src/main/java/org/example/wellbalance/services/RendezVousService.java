package org.example.wellbalance.services;

import org.example.wellbalance.models.RendezVous;
import org.example.wellbalance.models.TypeRendezVous;
import org.example.wellbalance.utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RendezVousService {

    private final Connection cnx;

    public RendezVousService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    public void ajouter(RendezVous r) {
        String sql = "INSERT INTO rendez_vous (date_rdv, heure_rdv, statut, remarque, type_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(r.getDateRdv()));
            ps.setTime(2, Time.valueOf(r.getHeureRdv()));
            ps.setString(3, r.getStatut());
            ps.setString(4, r.getRemarque());
            ps.setInt(5, r.getTypeRendezVous().getId());
            ps.executeUpdate();
            System.out.println("RendezVous ajouté avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur ajout RendezVous : " + e.getMessage());
        }
    }

    public List<RendezVous> afficher() {
        List<RendezVous> list = new ArrayList<>();

        String sql = """
                SELECT r.id, r.date_rdv, r.heure_rdv, r.statut, r.remarque,
                       t.id AS type_id, t.libelle, t.description, t.duree, t.prix, t.categorie
                FROM rendez_vous r
                JOIN type_rendezvous t ON r.type_id = t.id
                """;

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                TypeRendezVous t = new TypeRendezVous(
                        rs.getInt("type_id"),
                        rs.getString("libelle"),
                        rs.getString("description"),
                        rs.getInt("duree"),
                        rs.getDouble("prix"),
                        rs.getString("categorie")
                );

                RendezVous r = new RendezVous(
                        rs.getInt("id"),
                        rs.getDate("date_rdv").toLocalDate(),
                        rs.getTime("heure_rdv").toLocalTime(),
                        rs.getString("statut"),
                        rs.getString("remarque"),
                        t
                );

                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Erreur affichage RendezVous : " + e.getMessage());
        }

        return list;
    }

    public void modifier(RendezVous r) {
        String sql = "UPDATE rendez_vous SET date_rdv=?, heure_rdv=?, statut=?, remarque=?, type_id=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(r.getDateRdv()));
            ps.setTime(2, Time.valueOf(r.getHeureRdv()));
            ps.setString(3, r.getStatut());
            ps.setString(4, r.getRemarque());
            ps.setInt(5, r.getTypeRendezVous().getId());
            ps.setInt(6, r.getId());
            ps.executeUpdate();
            System.out.println("RendezVous modifié avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur modification RendezVous : " + e.getMessage());
        }
    }

    public boolean supprimer(int id) {
        if (cnx == null) {
            System.out.println("Connexion null.");
            return false;
        }

        String sql = "DELETE FROM rendez_vous WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Erreur suppression RendezVous : " + e.getMessage());
            return false;
        }
    }
}