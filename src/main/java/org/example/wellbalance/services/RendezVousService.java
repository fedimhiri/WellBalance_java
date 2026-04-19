package org.example.wellbalance.services;

import org.example.wellbalance.models.RendezVous;
import org.example.wellbalance.models.TypeRendezVous;
import org.example.wellbalance.utils.MyConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RendezVousService {

    private static final List<String> RENDEZ_VOUS_EMAIL_COLUMNS = List.of(
            "email_utilisateur",
            "user_email",
            "patient_email",
            "email"
    );
    private static final List<String> RENDEZ_VOUS_USER_ID_COLUMNS = List.of(
            "user_id",
            "utilisateur_id",
            "patient_id",
            "id_user"
    );
    private static final List<String> USER_TABLES = List.of(
            "user",
            "users",
            "utilisateur",
            "utilisateurs",
            "patient",
            "patients"
    );
    private static final List<String> USER_EMAIL_COLUMNS = List.of("email", "mail");

    private final Connection cnx;

    public RendezVousService() {
        cnx = MyConnection.getInstance().getCnx();
        synchroniserColonneStatut();
    }

    public void ajouter(RendezVous r) {
        String sql = "INSERT INTO rendez_vous (date_rdv, heure_rdv, statut, remarque, type_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(r.getDateRdv()));
            ps.setTime(2, Time.valueOf(r.getHeureRdv()));
            // Le statut est imposé côté métier à la création.
            ps.setString(3, RendezVous.STATUT_EN_COURS);
            ps.setString(4, r.getRemarque());
            ps.setInt(5, r.getTypeRendezVous().getId());
            ps.executeUpdate();
            System.out.println("RendezVous ajoute avec succes.");
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
            System.out.println("RendezVous modifie avec succes.");
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

    public boolean accepter(int id) {
        return mettreAJourStatut(id, RendezVous.STATUT_ACCEPTE);
    }

    public boolean refuser(int id) {
        return mettreAJourStatut(id, RendezVous.STATUT_REFUSE);
    }

    public Optional<String> trouverEmailUtilisateur(int rendezVousId) {
        if (cnx == null) {
            return Optional.empty();
        }

        Optional<String> emailDirect = trouverEmailDirectRendezVous(rendezVousId);
        if (emailDirect.isPresent()) {
            return emailDirect;
        }

        return trouverEmailDepuisRelationUtilisateur(rendezVousId);
    }

    public boolean mettreAJourStatut(int id, String statut) {
        String sql = "UPDATE rendez_vous SET statut=? WHERE id=? AND statut=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.setString(3, RendezVous.STATUT_EN_COURS);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur mise a jour statut RendezVous : " + e.getMessage());
            return false;
        }
    }

    public int compterTous() {
        return compterParRequete("SELECT COUNT(*) FROM rendez_vous");
    }

    public int compterParStatut(String statut) {
        String sql = "SELECT COUNT(*) FROM rendez_vous WHERE statut=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.out.println("Erreur comptage statut RendezVous : " + e.getMessage());
            return 0;
        }
    }

    public int compterAujourdhui() {
        String sql = "SELECT COUNT(*) FROM rendez_vous WHERE date_rdv = CURDATE()";
        return compterParRequete(sql);
    }

    private void synchroniserColonneStatut() {
        if (cnx == null) {
            return;
        }

        try {
            if (!colonneStatutExiste()) {
                try (Statement st = cnx.createStatement()) {
                    st.executeUpdate(
                            "ALTER TABLE rendez_vous ADD COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'en cours' AFTER heure_rdv"
                    );
                }
            } else {
                try (Statement st = cnx.createStatement()) {
                    st.executeUpdate(
                            "ALTER TABLE rendez_vous MODIFY COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'en cours'"
                    );
                }
            }

            try (Statement st = cnx.createStatement()) {
                // On corrige les anciennes lignes vides sans ecraser les rendez-vous deja traites.
                st.executeUpdate(
                        "UPDATE rendez_vous SET statut='en cours' WHERE statut IS NULL OR TRIM(statut)=''"
                );
            }
        } catch (SQLException e) {
            System.out.println("Synchronisation statut RendezVous ignoree : " + e.getMessage());
        }
    }

    private boolean colonneStatutExiste() throws SQLException {
        DatabaseMetaData metaData = cnx.getMetaData();

        try (ResultSet rs = metaData.getColumns(cnx.getCatalog(), null, "rendez_vous", "statut")) {
            return rs.next();
        }
    }

    private Optional<String> trouverEmailDirectRendezVous(int rendezVousId) {
        for (String emailColumn : RENDEZ_VOUS_EMAIL_COLUMNS) {
            if (!colonneExiste("rendez_vous", emailColumn)) {
                continue;
            }

            String sql = "SELECT " + emailColumn + " FROM rendez_vous WHERE id=?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, rendezVousId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Optional<String> email = nettoyerEmail(rs.getString(emailColumn));
                        if (email.isPresent()) {
                            return email;
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println("Recherche email rendez-vous ignoree : " + e.getMessage());
            }
        }

        return Optional.empty();
    }

    private Optional<String> trouverEmailDepuisRelationUtilisateur(int rendezVousId) {
        for (String userIdColumn : RENDEZ_VOUS_USER_ID_COLUMNS) {
            if (!colonneExiste("rendez_vous", userIdColumn)) {
                continue;
            }

            for (String userTable : USER_TABLES) {
                if (!tableExiste(userTable)) {
                    continue;
                }

                for (String emailColumn : USER_EMAIL_COLUMNS) {
                    if (!colonneExiste(userTable, emailColumn)) {
                        continue;
                    }

                    Optional<String> email = chercherEmailParJointure(rendezVousId, userIdColumn, userTable, emailColumn);
                    if (email.isPresent()) {
                        return email;
                    }
                }
            }
        }

        return Optional.empty();
    }

    private Optional<String> chercherEmailParJointure(int rendezVousId, String userIdColumn, String userTable, String emailColumn) {
        String sql = "SELECT u." + emailColumn + " FROM rendez_vous r JOIN " + userTable + " u ON r." + userIdColumn + "=u.id WHERE r.id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, rendezVousId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return nettoyerEmail(rs.getString(emailColumn));
                }
            }
        } catch (SQLException e) {
            System.out.println("Recherche email utilisateur ignoree : " + e.getMessage());
        }

        return Optional.empty();
    }

    private Optional<String> nettoyerEmail(String email) {
        return email == null || email.isBlank() ? Optional.empty() : Optional.of(email.trim());
    }

    private boolean tableExiste(String tableName) {
        try {
            DatabaseMetaData metaData = cnx.getMetaData();
            try (ResultSet rs = metaData.getTables(cnx.getCatalog(), null, tableName, new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean colonneExiste(String tableName, String columnName) {
        try {
            DatabaseMetaData metaData = cnx.getMetaData();
            try (ResultSet rs = metaData.getColumns(cnx.getCatalog(), null, tableName, columnName)) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private int compterParRequete(String sql) {
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.out.println("Erreur comptage RendezVous : " + e.getMessage());
            return 0;
        }
    }
}
