package tn.esprit.util;

import tn.esprit.models.User;

public class SessionManager {

    private static User currentUser = null;
    private static SessionManager instance = null;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Login user
    public void login(User user) {
        currentUser = user;
        System.out.println("Utilisateur connecte: " + user.getUsername());
    }

    // Logout user
    public void logout() {
        System.out.println("Deconnexion de: " + (currentUser != null ? currentUser.getUsername() : "inconnu"));
        currentUser = null;
    }

    // Get current user
    public User getCurrentUser() {
        return currentUser;
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Check if current user is doctor
    public boolean isDoctor() {
        return currentUser != null && currentUser.isDoctor();
    }

    // Check if current user is patient
    public boolean isPatient() {
        return currentUser != null && currentUser.isPatient();
    }

    // Get current user ID
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
}
