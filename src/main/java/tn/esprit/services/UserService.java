package tn.esprit.services;

import tn.esprit.models.User;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private Connection cnx = MyConnection.getInstance().getCnx();

    // Static login method (simple version for workshop)
    public User login(String email, String password) {
        // For workshop: use static users
        return User.login(email, password);
    }

    // Get user by ID
    public User getById(int id) {
        // Return static users if matching
        if (id == User.STATIC_DOCTOR.getId()) {
            return User.STATIC_DOCTOR;
        }
        if (id == User.STATIC_PATIENT.getId()) {
            return User.STATIC_PATIENT;
        }

        // Otherwise query database
        String req = "SELECT * FROM user WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get all users
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        // Add static users first
        users.add(User.STATIC_DOCTOR);
        users.add(User.STATIC_PATIENT);

        String req = "SELECT * FROM user WHERE id NOT IN (6, 7)";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Get all doctors
    public List<User> getAllDoctors() {
        List<User> doctors = new ArrayList<>();
        doctors.add(User.STATIC_DOCTOR);

        String req = "SELECT * FROM user WHERE roles LIKE '%ROLE_MEDCIN%' AND id != 6";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                doctors.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }

    // Get all patients
    public List<User> getAllPatients() {
        List<User> patients = new ArrayList<>();
        patients.add(User.STATIC_PATIENT);

        String req = "SELECT * FROM user WHERE roles LIKE '%ROLE_USER%' AND id != 7";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                patients.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    // Update last activity
    public void updateLastActivity(int userId) {
        String req = "UPDATE user SET last_activity = CURRENT_TIMESTAMP WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to map ResultSet to User
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setUsername(rs.getString("username"));
        user.setTelephone(rs.getString("telephone"));
        user.setRoles(rs.getString("roles"));
        user.setPassword(rs.getString("password"));
        user.setLastActivity(rs.getTimestamp("last_activity"));
        return user;
    }
}
