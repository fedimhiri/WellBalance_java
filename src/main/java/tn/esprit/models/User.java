package tn.esprit.models;

import java.sql.Timestamp;
import java.util.List;

public class User {
    private int id;
    private String email;
    private String username;
    private String telephone;
    private String roles;
    private String password;
    private Timestamp lastActivity;

    // Static users for login (like the database records)
    public static final User STATIC_DOCTOR = new User(
        6,
        "docteur.fedi@esprit.tn",
        "Dr. Fedi",
        "23223668",
        "[\"ROLE_MEDCIN\"]",
        "$2y$13$uPsqEiNiuHd7AttDhoG32.v/KdRXWure5hJKFq2Y/64uSkKUBqwLi"
    );

    public static final User STATIC_PATIENT = new User(
        7,
        "patient.amine@gmail.com",
        "Amine Kadri",
        "51644977",
        "[\"ROLE_USER\"]",
        "$2y$13$/i.C1ASvQepk2yaZh/iJUuBmvxYrnJotyXPo6S5LmhShiP1k01JZ."
    );

    public User() {}

    public User(int id, String email, String username, String telephone, String roles, String password) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.telephone = telephone;
        this.roles = roles;
        this.password = password;
    }

    // Check if user is doctor
    public boolean isDoctor() {
        return roles != null && roles.contains("ROLE_MEDCIN");
    }

    // Check if user is patient
    public boolean isPatient() {
        return roles != null && roles.contains("ROLE_USER");
    }

    // Simple login check (in real app, use password hashing)
    public static User login(String email, String password) {
        // Check static doctor
        if (STATIC_DOCTOR.getEmail().equals(email)) {
            // For demo purposes, accept any password or check specific password
            if (password.equals("doctor123") || password.equals("admin")) {
                return STATIC_DOCTOR;
            }
        }
        // Check static patient
        if (STATIC_PATIENT.getEmail().equals(email)) {
            if (password.equals("patient123") || password.equals("admin")) {
                return STATIC_PATIENT;
            }
        }
        return null;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Timestamp getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Timestamp lastActivity) {
        this.lastActivity = lastActivity;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", roles='" + roles + '\'' +
                '}';
    }

    // For displaying in UI
    public String getDisplayName() {
        return username;
    }

    // Get first letter for avatar
    public String getAvatarLetter() {
        if (username != null && !username.isEmpty()) {
            return String.valueOf(username.charAt(0)).toUpperCase();
        }
        return "?";
    }
}
