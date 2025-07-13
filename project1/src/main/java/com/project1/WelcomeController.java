package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

public class WelcomeController {

    @FXML
    private Button signInButton;

    @FXML
    private Button signUpButton;

    @FXML
    private void initialize() {
        signInButton.setOnAction(e -> handleSignIn(e));
        signUpButton.setOnAction(e -> handleSignUp(e));
    }

    private void handleSignIn(ActionEvent event) {
        SceneChanger.switchScene(event, "SignIn.fxml");
    }

    private void handleSignUp(ActionEvent event) {
        SceneChanger.switchScene(event, "SignUp.fxml");
    }
}
