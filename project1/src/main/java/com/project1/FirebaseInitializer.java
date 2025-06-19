package com.project1;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.IOException;
import java.io.InputStream;

/**
 * Initializes the Firebase application and sets up Firestore.
 * @author Utku
 */
public class FirebaseInitializer {

    /**
     * Initializes Firebase using the service account key file.
     *
     * @throws IOException if the key file cannot be read or Firebase fails to initialize.
     *@author Utku
     */
    public static void initialize() {
        try (InputStream serviceAccount =
                FirebaseInitializer.class.getClassLoader()
                        .getResourceAsStream("firebase/serviceAccountKey.json")) {

            if (serviceAccount == null) {
                throw new IllegalStateException("serviceAccountKey.json bulunamadı!");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("✅ Firebase bağlantısı BAŞARILI!");

        } catch (Exception e) {
            System.out.println("❌ Firebase bağlantısı BAŞARISIZ!");
            e.printStackTrace();
        }
    }
}
