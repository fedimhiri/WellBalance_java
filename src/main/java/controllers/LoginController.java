package controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import model.GoogleUser;
import model.User;
import netscape.javascript.JSObject;
import service.FaceRecognitionService;
import service.GoogleAuthService;
import service.RecaptchaService;
import service.UserService;
import service.UserSessionService;
import utils.RecaptchaHttpServer;
import utils.Session;
import utils.ThemeManager;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private WebView       recaptchaWebView;
    @FXML private Label         captchaErrorLabel;
    @FXML private Button        btnFaceLogin;

    private final UserService            userService        = new UserService();
    private final UserSessionService     userSessionService = new UserSessionService();
    private final GoogleAuthService      googleAuthService  = new GoogleAuthService();
    private final RecaptchaService       recaptchaService   = new RecaptchaService();
    private final FaceRecognitionService faceService        = new FaceRecognitionService();

    private String        recaptchaToken = "";
    private JavaConnector javaConnector;

    // ===== Inner class JavaConnector =====
    public class JavaConnector {
        public void setToken(String token) {
            System.out.println("reCAPTCHA token reçu: "
                    + token.substring(0, Math.min(20, token.length())) + "...");
            recaptchaToken = token;
        }
        public void resetToken() {
            System.out.println("reCAPTCHA token expiré");
            recaptchaToken = "";
        }
    }

    // ===== Initialisation =====

    @FXML
    public void initialize() {
        hideCaptchaError();
        setupRecaptcha();
        applyThemeToScene();
    }

    private void applyThemeToScene() {
        Platform.runLater(() -> {
            if (emailField != null && emailField.getScene() != null) {
                ThemeManager.applyTheme(emailField.getScene());
            }
        });
    }

    private void setupRecaptcha() {
        WebEngine engine = recaptchaWebView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));

        javaConnector = new JavaConnector();

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaConnector", javaConnector);
                System.out.println("JavaConnector injecté dans le WebView");
            }
            if (newState == Worker.State.FAILED) {
                System.err.println("Erreur chargement WebView: "
                        + engine.getLoadWorker().getException());
            }
        });

        String url = RecaptchaHttpServer.start();
        if (url != null) {
            System.out.println("Chargement du reCAPTCHA depuis: " + url);
            engine.load(url);
        } else {
            System.err.println("Impossible de démarrer le serveur reCAPTCHA local.");
        }
    }

    // ===== Login classique =====

    @FXML
    public void handleLogin(ActionEvent event) {
        hideCaptchaError();

        String email    = emailField.getText()    == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs requis", "Veuillez remplir tous les champs.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showAlert(Alert.AlertType.WARNING, "Email invalide", "Veuillez saisir une adresse email valide.");
            return;
        }

        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            showCaptchaError("Veuillez cocher le reCAPTCHA.");
            return;
        }

        if (!recaptchaService.verifyToken(recaptchaToken)) {
            showCaptchaError("Validation reCAPTCHA échouée. Veuillez réessayer.");
            resetCaptcha();
            return;
        }

        try {
            User user = userService.getUserByEmail(email);

            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Email ou mot de passe invalide.");
                resetCaptcha();
                return;
            }

            if (user.getIsBanned() == 1) {
                showAlert(Alert.AlertType.ERROR, "Compte banni", "Cet utilisateur est banni.");
                resetCaptcha();
                return;
            }

            boolean ok = userService.login(email, password);
            if (!ok) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Email ou mot de passe invalide.");
                resetCaptcha();
                return;
            }

            openSession(user, event);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la connexion : " + e.getMessage());
            resetCaptcha();
        }
    }

    // ===== Login par reconnaissance faciale =====

    @FXML
    public void handleFaceLogin(ActionEvent event) {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Email requis",
                    "Veuillez d'abord saisir votre email puis cliquez sur reconnaissance faciale.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showAlert(Alert.AlertType.WARNING, "Email invalide",
                    "Veuillez saisir une adresse email valide.");
            return;
        }

        try {
            User user = userService.getUserByEmail(email);

            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun compte trouvé avec cet email.");
                return;
            }

            if (user.getIsBanned() == 1) {
                showAlert(Alert.AlertType.ERROR, "Compte banni", "Cet utilisateur est banni.");
                return;
            }

            // Vérifier si l'utilisateur a enregistré son visage
            if (!faceService.hasFaceRegistered(user.getUsername())) {
                showAlert(Alert.AlertType.WARNING, "Visage non enregistré",
                        "Aucun visage enregistré pour ce compte. Veuillez vous inscrire d'abord.");
                return;
            }

            showAlert(Alert.AlertType.INFORMATION, "Reconnaissance faciale",
                    "La caméra va s'ouvrir. Regardez-la pendant quelques secondes.");

            // ✅ Thread séparé pour ne pas bloquer l'UI
            new Thread(() -> {
                boolean ok = faceService.verifyFace(user.getUsername());

                Platform.runLater(() -> {
                    if (ok) {
                        try {
                            openSession(user, event);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Échec",
                                "Visage non reconnu. Réessayez ou utilisez votre mot de passe.");
                    }
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ===== Login Google =====

    @FXML
    public void handleGoogleLogin(ActionEvent event) {
        try {
            GoogleUser googleUser = googleAuthService.authenticate();

            if (googleUser == null || googleUser.getEmail() == null
                    || googleUser.getEmail().isBlank()) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de récupérer les informations du compte Google.");
                return;
            }

            User user = userService.getUserByEmail(googleUser.getEmail());

            if (user == null) {
                user = new User();
                user.setEmail(googleUser.getEmail());

                String username = googleUser.getName();
                if (username == null || username.isBlank()) {
                    username = googleUser.getEmail().split("@")[0];
                }

                user.setUsername(username);
                user.setTelephone("000000");
                user.setPassword("GOOGLE_AUTH");
                user.setRoles("[\"ROLE_USER\"]");
                user.setIsBanned(0);

                userService.ajouterUtilisateurGoogle(user);
                user = userService.getUserByEmail(googleUser.getEmail());
            }

            if (user.getIsBanned() == 1) {
                showAlert(Alert.AlertType.ERROR, "Compte banni", "Cet utilisateur est banni.");
                return;
            }

            openSession(user, event);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Connexion Google impossible : " + e.getMessage());
        }
    }

    // ===== Helpers =====

    // ✅ Méthode commune : crée la session et redirige
    private void openSession(User user, ActionEvent event) throws Exception {
        Session.setCurrentUser(user);
        String token = userSessionService.createSession(user);
        Session.setCurrentSessionToken(token);
        redirectByRole(user, event);
    }

    private void redirectByRole(User user, ActionEvent event) {
        String roles = user.getRoles();
        if (roles != null && roles.contains("ROLE_ADMIN")) {
            loadPage("/fxml/admin_dashboard.fxml", event, "Admin Dashboard");
        } else {
            loadPage("/fxml/front.fxml", event, "Front Office");
        }
    }

    private void resetCaptcha() {
        recaptchaWebView.getEngine().reload();
        recaptchaToken = "";
    }

    @FXML
    public void goToRegister(ActionEvent event) {
        loadPage("/fxml/register.fxml", event, "Créer un compte");
    }

    @FXML
    public void goToForgotPassword(ActionEvent event) {
        loadPage("/fxml/forgot_password.fxml", event, "Mot de passe oublié");
    }

    private void loadPage(String fxmlPath, ActionEvent event, String title) {
        try {
            if (getClass().getResource(fxmlPath) == null) {
                throw new Exception("Fichier FXML introuvable : " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1500, 900);
            if (getClass().getResource("/css/style.css") != null) {
                scene.getStylesheets().add(
                        getClass().getResource("/css/style.css").toExternalForm()
                );
                ThemeManager.applyTheme(scene);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            Session.startInactivityTimer(scene, stage);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible de charger la page : " + e.getMessage());
        }
    }

    private void showCaptchaError(String message) {
        captchaErrorLabel.setText(message);
        captchaErrorLabel.setVisible(true);
        captchaErrorLabel.setManaged(true);
    }

    private void hideCaptchaError() {
        captchaErrorLabel.setText("");
        captchaErrorLabel.setVisible(false);
        captchaErrorLabel.setManaged(false);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}