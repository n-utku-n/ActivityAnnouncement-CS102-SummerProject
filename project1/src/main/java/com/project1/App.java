/// App.java
package com.project1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class that launches the JavaFX GUI.
 * @author Utku
 */
public class App extends Application {

    /**
     * Starts the JavaFX application by loading the Entry.fxml file.
     *
     * @param primaryStage The primary stage for this application.
     * @throws Exception if loading the FXML file fails.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/welcome.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("BilCall");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


     /**
     * Launches the JavaFX application.
     *
     * @param args the command line arguments.
     * @author Utku
     */
   public static void main(String[] args) {
        System.setProperty("prism.debug",   "false");
        System.setProperty("prism.verbose", "false");
        System.setProperty("javafx.debug",  "false");
        // 2) Firebase’i initialize et
        FirebaseInitializer.initialize();

        // 3) JavaFX uygulamasını başlat
        launch(args);
    }
}
