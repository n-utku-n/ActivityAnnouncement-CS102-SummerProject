/// App.java
package com.project1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Entry.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("BilCall");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        FirebaseInitializer.initialize();
        launch(args);
    }
}
