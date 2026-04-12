package service;

import model.User;
import utils.DataBaseConnection;
import utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private Connection cnx;

    public UserService() {
        cnx = DataBaseConnection.getConnection();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String sql = "INSERT INTO `user` (email, roles, password, username, telephone, is_banned) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
        String defaultRole = "[\"ROLE_USER\"]";

        ps.setString(1, user.getEmail());
        ps.setString(2, defaultRole);
        ps.setString(3, hashedPassword);
        ps.setString(4, user.getUsername());
        ps.setString(5, user.getTelephone());
        ps.setInt(6, 0);

        ps.executeUpdate();
        System.out.println("Utilisateur ajouté avec succès.");
    }

    @Override
    public void modifier(User user) throws SQLException {
        String sql = "UPDATE `user` SET email=?, roles=?, password=?, username=?, telephone=?, is_banned=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        String passwordToSave = user.getPassword();

        if (passwordToSave != null && !passwordToSave.isEmpty() && !passwordToSave.startsWith("$2")) {
            passwordToSave = PasswordUtils.hashPassword(passwordToSave);
        }

        ps.setString(1, user.getEmail());
        ps.setString(2, user.getRoles());
        ps.setString(3, passwordToSave);
        ps.setString(4, user.getUsername());
        ps.setString(5, user.getTelephone());
        ps.setInt(6, user.getIsBanned());
        ps.setInt(7, user.getId());

        ps.executeUpdate();
        System.out.println("Utilisateur modifié avec succès.");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `user` WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ps.executeUpdate();
        System.out.println("Utilisateur supprimé avec succès.");
    }

    @Override
    public List<User> afficher() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM `user`";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setRoles(rs.getString("roles"));
            u.setPassword(rs.getString("password"));
            u.setUsername(rs.getString("username"));
            u.setTelephone(rs.getString("telephone"));
            u.setIsBanned(rs.getInt("is_banned"));
            users.add(u);
        }

        return users;
    }

    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM `user` WHERE email=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setRoles(rs.getString("roles"));
            u.setPassword(rs.getString("password"));
            u.setUsername(rs.getString("username"));
            u.setTelephone(rs.getString("telephone"));
            u.setIsBanned(rs.getInt("is_banned"));

            System.out.println("User trouvé : " + u.getEmail());
            System.out.println("Roles DB : " + u.getRoles());
            System.out.println("Password DB : " + u.getPassword());

            return u;
        }

        System.out.println("Aucun utilisateur trouvé pour : " + email);
        return null;
    }

    public boolean login(String email, String password) throws SQLException {
        User user = getUserByEmail(email);

        if (user == null) {
            System.out.println("Login : utilisateur introuvable");
            return false;
        }

        if (user.getIsBanned() == 1) {
            System.out.println("Login : utilisateur banni");
            return false;
        }

        String storedPassword = user.getPassword();

        if (storedPassword == null || storedPassword.isEmpty()) {
            System.out.println("Login : mot de passe vide en base");
            return false;
        }

        if (!storedPassword.startsWith("$2")) {
            System.out.println("Login : mot de passe non BCrypt -> " + storedPassword);
            return false;
        }

        try {
            boolean result = PasswordUtils.checkPassword(password, storedPassword);
            System.out.println("BCrypt result : " + result);
            return result;
        } catch (Exception e) {
            System.out.println("Erreur BCrypt : " + e.getMessage());
            return false;
        }
    }

    public boolean isAdmin(User user) {
        if (user == null) {
            System.out.println("isAdmin : user = null");
            return false;
        }

        if (user.getRoles() == null) {
            System.out.println("isAdmin : roles = null");
            return false;
        }

        String roles = user.getRoles().trim();
        System.out.println("isAdmin - roles = " + roles);

        boolean result = roles.contains("ROLE_ADMIN");
        System.out.println("isAdmin - contains ROLE_ADMIN ? " + result);

        return result;
    }
    public void changerStatutBan(int id, int isBanned) throws SQLException {
        String sql = "UPDATE `user` SET is_banned=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, isBanned);
        ps.setInt(2, id);
        ps.executeUpdate();
    }
}