package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class MainDashboardController {

    @FXML
    private Button profileButton;

    @FXML
    private TextField searchField;

    @FXML
    private GridPane eventGrid;

    @FXML
    private void handleProfile() {
        System.out.println("👤 Profil butonuna tıklandı (gelecekte profil ekranı açılacak).");
    }
}
