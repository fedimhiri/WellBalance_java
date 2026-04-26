package service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import model.GoogleUser;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

public class GoogleAuthService {

    private static final String APPLICATION_NAME = "WellBalance";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public GoogleUser authenticate() throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = getClass().getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new Exception("credentials.json introuvable dans src/main/resources");
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(in)
        );

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                Collections.singletonList("https://www.googleapis.com/auth/userinfo.email")
        )
                .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver)
                .authorize("user");

        Oauth2 oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Userinfo userInfo = oauth2.userinfo().get().execute();

        return new GoogleUser(userInfo.getEmail(), userInfo.getName());
    }
    public void clearSavedGoogleSession() {
        File tokensFolder = new File("tokens");
        deleteRecursively(tokensFolder);
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursively(child);
                }
            }
        }

        file.delete();
    }
}