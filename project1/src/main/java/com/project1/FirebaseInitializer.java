package com.project1;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

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
                    .setStorageBucket("project1-9c22f.firebasestorage.app")  // 🔸 BU SATIRI EKLE
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("✅ Firebase bağlantısı BAŞARILI!");

        } catch (Exception e) {
            System.out.println("❌ Firebase bağlantısı BAŞARISIZ!");
            e.printStackTrace();
        }
    }

        private static Firestore db;

        /**
         * Returns the initialized Firestore instance.
         */
        public static Firestore getFirestore() {
            if (db == null) {
                db = FirestoreClient.getFirestore();
            }
            return db;
        }
}
