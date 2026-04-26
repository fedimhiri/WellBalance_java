package utils;

import javafx.scene.Scene;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String THEME_KEY = "app_theme";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static boolean isDarkMode = prefs.getBoolean(THEME_KEY, false);

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void toggleTheme(Scene scene) {
        setDarkMode(!isDarkMode, scene);
    }

    public static void setDarkMode(boolean dark, Scene scene) {
        isDarkMode = dark;
        prefs.putBoolean(THEME_KEY, isDarkMode);
        applyTheme(scene);
    }

    public static void applyTheme(Scene scene) {
        if (scene == null) return;
        
        if (isDarkMode) {
            if (!scene.getRoot().getStyleClass().contains("dark-mode")) {
                scene.getRoot().getStyleClass().add("dark-mode");
            }
        } else {
            scene.getRoot().getStyleClass().remove("dark-mode");
        }
    }
}
