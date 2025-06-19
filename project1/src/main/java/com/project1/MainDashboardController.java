package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Controller class for the main dashboard accessible by student and club_manager roles.
 * Displays user profile, event list, and allows searching.
 * @author Utku
 */
public class MainDashboardController {

    @FXML
    private Button profileButton;

    @FXML
    private TextField searchField;

    @FXML
    private GridPane eventGrid;

    /**
     * TODO: not implemented
     * @author Utku
     */
    @FXML
    private void handleProfile() {
        System.out.println("👤 Profil butonuna tıklandı (gelecekte profil ekranı açılacak).");
    }
}
