/// EntryController.java
package com.project1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class EntryController {

    @FXML
    private void handleSignIn(ActionEvent event) {
        System.out.println("Sign In tıklandı!");
        try {
            Parent signInRoot = FXMLLoader.load(getClass().getResource("/views/SignIn.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(signInRoot));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        System.out.println("Sign Up tıklandı!");
        try {
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("/views/SignUp.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(signUpRoot));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
