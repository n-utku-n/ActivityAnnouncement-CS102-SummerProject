package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import com.project1.SceneChanger;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;

import org.json.JSONObject;

// ↓ Aşağısını ekledim:
import javafx.fxml.FXMLLoader;
import com.project1.UserModel;
import com.project1.MainDashboardController;
import com.project1.AdminDashboardController;  // Eğer admin dashboard controller’ınız farklıysa ona göre güncelleyin

/**
 * Controller class for handling user sign-in logic.
 * <p>
 * Authenticates the user via Firebase Authentication REST API and
 * redirects to the appropriate dashboard based on role retrieved from Firestore.
 * </p>
 *
 * @author Utku
 */
public class SignInController {

    @FXML
    private TextField emailField;

    @FXML
    private Label errorLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        SceneChanger.switchScene(event, "forgot_password.fxml");
    }

    private static final String API_KEY = "AIzaSyDYluEpPgovtKRDW5bjIMMg4BNLgjy52YM";
    

    /**
     * Handles sign-in button click.
     * Validates user input, sends request to Firebase Authentication API,
     * and redirects user to appropriate dashboard if successful.
     *
     * @param event the ActionEvent triggered by the Sign In button.
     */
    @FXML
    private void handleSignIn(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("❗ E-mail ve şifre boş olamaz.");
            errorLabel.setText("Email and password cannot be empty.");
            return;
        }

        try {
            URL url = new URL(
                "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" 
                + API_KEY
            );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JSONObject requestData = new JSONObject();
            requestData.put("email", email);
            requestData.put("password", password);
            requestData.put("returnSecureToken", true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = new String(
                    conn.getInputStream().readAllBytes(), 
                    StandardCharsets.UTF_8
                );
                JSONObject responseJson = new JSONObject(response);
                String uid = responseJson.getString("localId");
                System.out.println("✅ Giriş başarılı. UID: " + uid);

                // Firestore’dan kullanıcı dokümanını çek
                Firestore db = FirestoreClient.getFirestore();
                DocumentReference docRef = db.collection("users").document(uid);

                try {
                    DocumentSnapshot document = docRef.get().get(); // Blocking call
                    if (document.exists()) {
                        // ① UserModel’a dönüştür
                        UserModel userModel = document.toObject(UserModel.class);

                        // ② Rolü kontrol et
                        String role = document.getString("role");
                        System.out.println("🎯 Kullanıcı rolü: " + role);
                        errorLabel.setText(""); // Hata mesajı temizlensin

                        // ③ Sahne geçişi + UserModel aktarımı
                        switch (role) {
                            case "student":
                            case "club_manager": {
                                FXMLLoader loader = 
                                    SceneChanger.switchScene(event, "main_dashboard.fxml");
                                MainDashboardController mdc = loader.getController();
                                mdc.setLoggedInUser(userModel);
                                break;
                            }
                            case "admin": {
                                FXMLLoader loader2 = 
                                    SceneChanger.switchScene(event, "admin_dashboard.fxml");
                                AdminDashboardController adc = loader2.getController();
                                adc.setLoggedInUser(userModel);
                                break;
                            }
                            default:
                                System.out.println("⚠️ Tanımsız rol: " + role);
                                errorLabel.setText("Unknown role. Please contact support.");
                        }
                    } else {
                        System.out.println("❌ Firestore’da kullanıcı bulunamadı.");
                        errorLabel.setText("User record not found.");
                    }
                } catch (Exception e) {
                    System.out.println("❌ Firestore'dan rol alınamadı: " + e.getMessage());
                    errorLabel.setText("Error loading user profile.");
                }

            } else {
                String errorResponse = new String(
                    conn.getErrorStream().readAllBytes(), 
                    StandardCharsets.UTF_8
                );
                JSONObject errorJson = new JSONObject(errorResponse);
                String errorMessage = errorJson.getJSONObject("error").getString("message");
                System.out.println("❌ Giriş başarısız: " + errorMessage);

                switch (errorMessage) {
                    case "EMAIL_NOT_FOUND":
                    case "INVALID_PASSWORD":
                        errorLabel.setText("Incorrect email or password.");
                        break;
                    case "USER_DISABLED":
                        errorLabel.setText("This account has been disabled.");
                        break;
                    default:
                        errorLabel.setText("Login failed. Please try again.");
                        break;
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Hata: " + e.getMessage());
            errorLabel.setText("An unexpected error occurred. Please try again.");
        }
    }
}