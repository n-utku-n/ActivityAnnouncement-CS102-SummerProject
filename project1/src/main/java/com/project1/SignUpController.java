package com.project1;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignUpController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private TextField idField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleSignUp() {
        String name = nameField.getText();
        String surname = surnameField.getText();
        String studentId = idField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (!email.endsWith("@ug.bilkent.edu.tr")) {
            System.out.println("⚠️ Sadece Bilkent mail adresiyle kayıt olunabilir.");
            return;
        }

        if (password.length() < 6) {
            System.out.println("⚠️ Şifre en az 6 karakter olmalıdır.");
            return;
        }

        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setDisplayName(name + " " + surname);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            System.out.println("✅ Kullanıcı başarıyla oluşturuldu: " + userRecord.getUid());

            // İsteğe bağlı: Firestore'da öğrenci ID'si vb. bilgiler saklanabilir

        } catch (Exception e) {
            System.out.println("❌ Kayıt başarısız: " + e.getMessage());
        }
    }
}
