/// SignInController.java
package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignInController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleSignIn() {
        System.out.println("Giriş: " + emailField.getText());
        // Firebase Auth işlemi buraya eklenecek
    }
}