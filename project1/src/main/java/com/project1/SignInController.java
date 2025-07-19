package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;

import com.project1.SceneChanger;
import com.project1.UserModel;
import com.project1.MainDashboardController;
import com.project1.AdminDashboardController;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;

public class SignInController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private static final String API_KEY = "AIzaSyDYluEpPgovtKRDW5bjIMMg4BNLgjy52YM";

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        SceneChanger.switchScene(event, "forgot_password.fxml");
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Email and password cannot be empty.");
            return;
        }

        try {
            // 1) Firebase Auth REST API ile oturum açma
            URL url = new URL(
                "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
                + API_KEY
            );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JSONObject req = new JSONObject();
            req.put("email", email);
            req.put("password", password);
            req.put("returnSecureToken", true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(req.toString().getBytes(StandardCharsets.UTF_8));
            }

            // 2) Başarılıysa JSON’dan UID’yi al
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String resp = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JSONObject respJson = new JSONObject(resp);
                String uid = respJson.getString("localId");

                // 3) Firestore’dan users/{uid} belgesini çek
                Firestore db = FirestoreClient.getFirestore();
                DocumentReference docRef = db.collection("users").document(uid);
                DocumentSnapshot document = docRef.get().get();

                if (document.exists()) {
                    // 4) UserModel’a dönüştür ve UID ata
                    UserModel userModel = document.toObject(UserModel.class);
                    userModel.setUid(uid);

                    String role = document.getString("role");

                    // 5) club_manager ise önce user dokümanındaki clubId/Name’e bak, boşsa clubs koleksiyonunda managerId’ye göre ara
                    if ("club_manager".equals(role)) {
                        String clubId   = document.getString("clubId");
                        String clubName = document.getString("clubName");
                        if (clubId == null || clubId.isEmpty()) {
                            QuerySnapshot clubSnap = db.collection("clubs")
                                .whereArrayContains("managers", uid)
                                .get().get();
                            if (!clubSnap.isEmpty()) {
                                DocumentSnapshot clubDoc = clubSnap.getDocuments().get(0);
                                clubId   = clubDoc.getId();
                                clubName = clubDoc.getString("name");
                            }
                        }
                        userModel.setClubId(clubId);
                        userModel.setClubName(clubName);
                    }

                    // 6) Hata mesajını temizle ve rollere göre sahne geçişi yap
                    errorLabel.setText("");
                    switch (role) {
                        case "student":
                        case "club_manager": {
                            FXMLLoader loader = SceneChanger.switchScene(event, "main_dashboard.fxml");
                            MainDashboardController mdc = loader.getController();
                            mdc.setLoggedInUser(userModel);
                            break;
                        }
                        case "admin": {
                            FXMLLoader loader2 = SceneChanger.switchScene(event, "admin_dashboard.fxml");
                            AdminDashboardController adc = loader2.getController();
                            adc.setLoggedInUser(userModel);
                            break;
                        }
                        default:
                            errorLabel.setText("Unknown role. Please contact support.");
                    }
                } else {
                    errorLabel.setText("User record not found.");
                }
            } else {
                // Başarısızsa hata mesajını al ve göster
                String errResp = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                JSONObject errJson = new JSONObject(errResp).getJSONObject("error");
                String msg = errJson.getString("message");
                if ("EMAIL_NOT_FOUND".equals(msg) || "INVALID_PASSWORD".equals(msg)) {
                    errorLabel.setText("Incorrect email or password.");
                } else if ("USER_DISABLED".equals(msg)) {
                    errorLabel.setText("This account has been disabled.");
                } else {
                    errorLabel.setText("Login failed. Please try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("An unexpected error occurred. Please try again.");
        }
    }
}