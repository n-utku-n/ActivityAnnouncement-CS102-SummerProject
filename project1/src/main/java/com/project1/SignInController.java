package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;

import org.json.JSONObject;

public class SignInController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private static final String API_KEY = "AIzaSyDYluEpPgovtKRDW5bjIMMg4BNLgjy52YM";

    @FXML
    private void handleSignIn(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("❗ E-mail ve şifre boş olamaz.");
            return;
        }

        try {
            URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JSONObject requestData = new JSONObject();
            requestData.put("email", email);
            requestData.put("password", password);
            requestData.put("returnSecureToken", true);

            OutputStream os = conn.getOutputStream();
            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                JSONObject responseJson = new JSONObject(response);
                String uid = responseJson.getString("localId");
                System.out.println("✅ Giriş başarılı. UID: " + uid);

                Firestore db = FirestoreClient.getFirestore();
                DocumentReference docRef = db.collection("users").document(uid);

                try {
                    DocumentSnapshot document = docRef.get().get(); // Blocking call
                    if (document.exists()) {
                        String role = document.getString("role");
                        System.out.println("🎯 Kullanıcı rolü: " + role);

                        switch (role) {
                            case "student":
                            case "club_manager":
                                SceneChanger.switchScene(event, "main_dashboard.fxml");
                                break;
                            case "admin":
                                SceneChanger.switchScene(event, "admin_dashboard.fxml");
                                break;
                            default:
                                System.out.println("⚠️ Tanımsız rol: " + role);
                        }
                    } else {
                        System.out.println("❌ Firestore’da kullanıcı bulunamadı.");
                    }
                } catch (Exception e) {
                    System.out.println("❌ Firestore'dan rol alınamadı: " + e.getMessage());
                }

            } else {
                InputStream es = conn.getErrorStream();
                String errorResponse = new String(es.readAllBytes(), StandardCharsets.UTF_8);
                JSONObject errorJson = new JSONObject(errorResponse);
                String errorMessage = errorJson.getJSONObject("error").getString("message");
                System.out.println("❌ Giriş başarısız: " + errorMessage);
            }

        } catch (Exception e) {
            System.out.println("❌ Hata: " + e.getMessage());
        }
    }
}
