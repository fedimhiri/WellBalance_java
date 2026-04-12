package utils;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.User;
import service.UserSessionService;

public class Session {

    private static User currentUser;
    private static String currentSessionToken;
    private static PauseTransition inactivityTimer;

    private static final int TIMEOUT_SECONDS = 1000000;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static String getCurrentSessionToken() {
        return currentSessionToken;
    }

    public static void setCurrentSessionToken(String currentSessionToken) {
        Session.currentSessionToken = currentSessionToken;
    }

    public static boolean isAdmin() {
        return currentUser != null
                && currentUser.getRoles() != null
                && currentUser.getRoles().contains("ROLE_ADMIN");
    }

    public static void clear() {
        currentUser = null;
        currentSessionToken = null;
        stopInactivityTimer();
    }

    public static void startInactivityTimer(Scene scene, Stage stage) {
        stopInactivityTimer();

        inactivityTimer = new PauseTransition(Duration.seconds(TIMEOUT_SECONDS));
        inactivityTimer.setOnFinished(event -> autoLogout(stage));

        scene.addEventFilter(javafx.scene.input.MouseEvent.ANY, event -> resetInactivityTimer());
        scene.addEventFilter(javafx.scene.input.KeyEvent.ANY, event -> resetInactivityTimer());
        scene.addEventFilter(javafx.scene.input.ScrollEvent.ANY, event -> resetInactivityTimer());

        resetInactivityTimer();
    }

    public static void resetInactivityTimer() {
        if (inactivityTimer != null) {
            inactivityTimer.playFromStart();
        }
    }

    public static void stopInactivityTimer() {
        if (inactivityTimer != null) {
            inactivityTimer.stop();
            inactivityTimer = null;
        }
    }

    private static void autoLogout(Stage stage) {
        Platform.runLater(() -> {
            try {
                UserSessionService userSessionService = new UserSessionService();

                if (currentSessionToken != null) {
                    userSessionService.closeSession(currentSessionToken);
                }

                clear();

                FXMLLoader loader = new FXMLLoader(Session.class.getResource("/fxml/login.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root, 1400, 850);

                if (Session.class.getResource("/css/style.css") != null) {
                    scene.getStylesheets().add(
                            Session.class.getResource("/css/style.css").toExternalForm()
                    );
                }

                stage.setTitle("Connexion");
                stage.setScene(scene);
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}