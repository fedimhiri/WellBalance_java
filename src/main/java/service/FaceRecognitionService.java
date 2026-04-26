package service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class FaceRecognitionService {

    private static final String PYTHON_CMD = "python";
    private static final String FACES_DIR  = "faces";

    private static String getScriptPath(String scriptName) {
        String projectDir = System.getProperty("user.dir");
        // ✅ Utilise File pour construire le chemin proprement
        File script = new File(projectDir,
                "src" + File.separator +
                        "main" + File.separator +
                        "resources" + File.separator +
                        "python" + File.separator +
                        scriptName);
        System.out.println("[Face] Chemin absolu : " + script.getAbsolutePath());
        return script.getAbsolutePath(); // ✅ getAbsolutePath() normalise les séparateurs
    }

    // ===== API publique =====

    public boolean registerFace(String username) {
        String script = getScriptPath("face_register.py");
        System.out.println("[Face] Script register : " + script);
        System.out.println("[Face] Lancement enregistrement pour : " + username);
        return runScript(script, username);
    }

    public boolean verifyFace(String username) {
        String script = getScriptPath("face_verify.py");
        System.out.println("[Face] Script verify : " + script);
        System.out.println("[Face] Lancement vérification pour : " + username);
        return runScript(script, username);
    }

    public boolean hasFaceRegistered(String username) {
        String projectDir = System.getProperty("user.dir");
        File faceFile = new File(projectDir
                + File.separator + FACES_DIR
                + File.separator + username + ".npy");
        boolean exists = faceFile.exists();
        System.out.println("[Face] Chemin cherché   : " + faceFile.getAbsolutePath());
        System.out.println("[Face] Visage enregistré : " + exists);
        return exists;
    }

    public boolean deleteFace(String username) {
        String projectDir = System.getProperty("user.dir");
        File faceFile = new File(projectDir
                + File.separator + FACES_DIR
                + File.separator + username + ".npy");
        if (faceFile.exists()) {
            boolean deleted = faceFile.delete();
            System.out.println("[Face] Suppression visage " + username + " : " + deleted);
            return deleted;
        }
        System.out.println("[Face] Aucun fichier à supprimer pour : " + username);
        return false;
    }

    public String getFacePath(String username) {
        String projectDir = System.getProperty("user.dir");
        return projectDir
                + File.separator + FACES_DIR
                + File.separator + username + ".npy";
    }

    // ===== Exécution du script Python =====

    private boolean runScript(String scriptPath, String username) {
        try {
            File script = new File(scriptPath);

            // Vérifier que le script existe
            if (!script.exists()) {
                System.err.println("[Face] Script introuvable : "
                        + script.getAbsolutePath());
                System.err.println("[Face] Répertoire courant : "
                        + System.getProperty("user.dir"));
                return false;
            }

            // ✅ Utiliser le chemin absolu du script
            ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_CMD,
                    script.getAbsolutePath(),
                    username
            );
            pb.redirectErrorStream(true);

            // ✅ Répertoire de travail = racine du projet
            pb.directory(new File(System.getProperty("user.dir")));

            Process process = pb.start();

            // Lire la sortie du script en temps réel
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Python] " + line);
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            System.out.println("[Face] Exit code : " + exitCode);
            System.out.println("[Face] Sortie complète :\n" + output);

            // ✅ Succès si le script a imprimé "SUCCESS"
            return output.toString().contains("SUCCESS");

        } catch (Exception e) {
            System.err.println("[Face] Erreur lors de l'exécution : "
                    + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}