package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private Label infoLabel;

    private static final String API_KEY = "AIzaSyDYluEpPgovtKRDW5bjIMMg4BNLgjy52YM"; // kendi key'inse değiştir

    @FXML
    private void handleSendResetLink(ActionEvent event) {
        String email = emailField.getText().trim();

        if (!email.endsWith("@ug.bilkent.edu.tr")) {
            infoLabel.setText("Please use your Bilkent email address.");
            return;
        }

        try {
            URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JSONObject request = new JSONObject();
            request.put("requestType", "PASSWORD_RESET");
            request.put("email", email);

            OutputStream os = conn.getOutputStream();
            os.write(request.toString().getBytes(StandardCharsets.UTF_8));
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                infoLabel.setText("✅ Check your email for the reset link.");
            } else {
                infoLabel.setText("❌ Error sending reset link.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            infoLabel.setText("❌ Unexpected error occurred.");
        }
    }
}
