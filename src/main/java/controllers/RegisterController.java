package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import service.FaceRecognitionService;
import service.UserService;

public class RegisterController {

    @FXML private TextField     emailField;
    @FXML private TextField     usernameField;
    @FXML private TextField     telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox      agreeTermsCheckBox;

    @FXML private Label emailError;
    @FXML private Label usernameError;
    @FXML private Label telephoneError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label termsError;

    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label       passwordStrengthLabel;

    private final UserService            userService  = new UserService();
    private final FaceRecognitionService faceService  = new FaceRecognitionService();

    // Garde l'username après inscription pour l'enregistrement facial
    private String registeredUsername = null;

    @FXML
    public void initialize() {
        emailField.textProperty().addListener((o, oldVal, newVal)
                -> clearError(emailField, emailError));
        usernameField.textProperty().addListener((o, oldVal, newVal)
                -> clearError(usernameField, usernameError));
        telephoneField.textProperty().addListener((o, oldVal, newVal)
                -> clearError(telephoneField, telephoneError));

        passwordField.textProperty().addListener((o, oldVal, newVal) -> {
            clearError(passwordField, passwordError);
            updatePasswordStrength(newVal);
        });

        confirmPasswordField.textProperty().addListener((o, oldVal, newVal)
                -> clearError(confirmPasswordField, confirmPasswordError));

        agreeTermsCheckBox.selectedProperty().addListener((o, oldVal, newVal) -> {
            if (newVal) clearError(null, termsError);
        });

        updatePasswordStrength("");
    }

    // ===== Register =====

    @FXML
    public void handleRegister(ActionEvent event) {
        String email           = emailField.getText().trim();
        String username        = usernameField.getText().trim();
        String telephone       = telephoneField.getText().trim();
        String password        = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        boolean valid = true;

        // Email
        if (email.isEmpty()) {
            showFieldError(emailField, emailError, "L'email est obligatoire.");
            valid = false;
        } else if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showFieldError(emailField, emailError, "Format d'email invalide.");
            valid = false;
        }

        // Username
        if (username.isEmpty()) {
            showFieldError(usernameField, usernameError, "Le nom d'utilisateur est obligatoire.");
            valid = false;
        } else if (username.length() < 3) {
            showFieldError(usernameField, usernameError, "Minimum 3 caractères.");
            valid = false;
        }

        // Téléphone
        if (telephone.isEmpty()) {
            showFieldError(telephoneField, telephoneError, "Le téléphone est obligatoire.");
            valid = false;
        } else if (!telephone.matches("\\d{8}")) {
            showFieldError(telephoneField, telephoneError, "Exactement 8 chiffres requis.");
            valid = false;
        }

        // Mot de passe
        if (password.isEmpty()) {
            showFieldError(passwordField, passwordError, "Le mot de passe est obligatoire.");
            valid = false;
        } else if (password.length() < 6) {
            showFieldError(passwordField, passwordError, "Minimum 6 caractères.");
            valid = false;
        } else if (calculatePasswordStrength(password) < 0.5) {
            showFieldError(passwordField, passwordError, "Le mot de passe est trop faible.");
            valid = false;
        }

        // Confirmation
        if (confirmPassword.isEmpty()) {
            showFieldError(confirmPasswordField, confirmPasswordError,
                    "Veuillez confirmer le mot de passe.");
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            showFieldError(confirmPasswordField, confirmPasswordError,
                    "Les mots de passe ne correspondent pas.");
            valid = false;
        }

        // Conditions
        if (!agreeTermsCheckBox.isSelected()) {
            showFieldError(null, termsError, "Vous devez accepter les conditions.");
            valid = false;
        }

        if (!valid) return;

        try {
            // Email déjà utilisé ?
            User existingUser = userService.getUserByEmail(email);
            if (existingUser != null) {
                showFieldError(emailField, emailError, "Cet email est déjà utilisé.");
                return;
            }

            // ✅ Création du compte
            User user = new User(email, password, username, telephone);
            userService.ajouter(user);

            registeredUsername = username;

            // ✅ Proposer l'enregistrement facial après inscription
            proposerEnregistrementFacial(event);

        } catch (Exception e) {
            e.printStackTrace();
            showFieldError(null, termsError, "Erreur : " + e.getMessage());
        }
    }

    // ===== Reconnaissance faciale =====

    private void proposerEnregistrementFacial(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reconnaissance faciale");
        alert.setHeaderText("Voulez-vous activer la connexion par reconnaissance faciale ?");
        alert.setContentText(
                "Cela vous permettra de vous connecter avec votre visage.\n" +
                        "Vous pouvez toujours le faire plus tard depuis votre profil."
        );

        ButtonType btnOui = new ButtonType("Oui, activer");
        ButtonType btnNon = new ButtonType("Non, plus tard", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnOui, btnNon);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnOui) {
                lancerEnregistrementFacial(event);
            } else {
                // Pas de reconnaissance faciale → aller directement au login
                loadPage("/fxml/login.fxml", event, "Connexion");
            }
        });
    }

    private void lancerEnregistrementFacial(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Caméra",
                "La caméra va s'ouvrir.\n" +
                        "Appuyez sur ESPACE pour capturer votre visage.\n" +
                        "Appuyez sur Q pour annuler."
        );

        // ✅ Thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            boolean ok = faceService.registerFace(registeredUsername);

            Platform.runLater(() -> {
                if (ok) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Visage enregistré avec succès !\n" +
                                    "Vous pouvez maintenant vous connecter avec votre visage."
                    );
                } else {
                    showAlert(Alert.AlertType.WARNING, "Non enregistré",
                            "Visage non enregistré.\n" +
                                    "Vous pouvez toujours vous connecter avec votre mot de passe."
                    );
                }
                // Dans tous les cas → aller au login
                loadPage("/fxml/login.fxml", event, "Connexion");
            });
        }).start();
    }

    // ===== Force du mot de passe =====

    private void updatePasswordStrength(String password) {
        if (passwordStrengthBar == null || passwordStrengthLabel == null) return;

        double strength = calculatePasswordStrength(password);
        passwordStrengthBar.setProgress(strength);
        passwordStrengthBar.getStyleClass().removeAll(
                "strength-weak", "strength-medium", "strength-strong", "strength-empty"
        );

        if (password == null || password.isEmpty()) {
            passwordStrengthBar.getStyleClass().add("strength-empty");
            passwordStrengthLabel.setText("Vide");
            passwordStrengthLabel.setStyle(
                    "-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold;"
            );
        } else if (strength < 0.4) {
            passwordStrengthBar.getStyleClass().add("strength-weak");
            passwordStrengthLabel.setText("Faible");
            passwordStrengthLabel.setStyle(
                    "-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: bold;"
            );
        } else if (strength < 0.75) {
            passwordStrengthBar.getStyleClass().add("strength-medium");
            passwordStrengthLabel.setText("Moyen");
            passwordStrengthLabel.setStyle(
                    "-fx-text-fill: #f59e0b; -fx-font-size: 12px; -fx-font-weight: bold;"
            );
        } else {
            passwordStrengthBar.getStyleClass().add("strength-strong");
            passwordStrengthLabel.setText("Fort");
            passwordStrengthLabel.setStyle(
                    "-fx-text-fill: #16a34a; -fx-font-size: 12px; -fx-font-weight: bold;"
            );
        }
    }

    private double calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0.0;

        int score = 0;
        if (password.length() >= 6)              score++;
        if (password.length() >= 8)              score++;
        if (password.matches(".*[A-Z].*"))       score++;
        if (password.matches(".*[a-z].*"))       score++;
        if (password.matches(".*\\d.*"))         score++;
        if (password.matches(".*[^a-zA-Z0-9].*")) score++;

        return score / 6.0;
    }

    // ===== Navigation =====

    @FXML
    public void goToLogin(ActionEvent event) {
        loadPage("/fxml/login.fxml", event, "Connexion");
    }

    private void loadPage(String fxmlPath, ActionEvent event, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1500, 900);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Helpers =====

    private void showFieldError(Control field, Label errorLabel, String message) {
        if (field != null) {
            field.setStyle(
                    "-fx-border-color: #e74c3c; -fx-border-width: 1.5; " +
                            "-fx-border-radius: 8; -fx-background-radius: 8;"
            );
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void clearError(Control field, Label errorLabel) {
        if (field != null) field.setStyle("");
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}