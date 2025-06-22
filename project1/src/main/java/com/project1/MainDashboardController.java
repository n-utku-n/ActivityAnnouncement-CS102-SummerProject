package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Node;

import java.util.List;

/**
 * Controller class for the main dashboard accessible by student and club_manager roles.
 * Displays user profile, event list, and allows searching.
 * @author Utku
 */
public class MainDashboardController {

    @FXML
    private VBox eventCardContainer;

    @FXML
    private Button profileButton;

    @FXML
    private TextField searchField;

    @FXML
    private GridPane eventGrid;

    @FXML
    private VBox mainEventContainer; // FXML'deki container için

    @FXML
    public void initialize() {
        loadEvents();  // initialize içinde SADECE çağırıyoruz
    }

    private void loadEvents() {
        mainEventContainer.getChildren().clear();

        try {
            List<QueryDocumentSnapshot> documents = FirestoreClient
                    .getFirestore()
                    .collection("events")
                    .get()
                    .get()
                    .getDocuments();

            for (QueryDocumentSnapshot doc : documents) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
                HBox eventCard = loader.load();

                EventCardController controller = loader.getController();
                controller.setData(doc.getId(), doc.getData());

                mainEventContainer.getChildren().add(eventCard);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * TODO: not implemented
     * @author Utku
     */
    @FXML
    private void handleProfile() {
        System.out.println("👤 Profil butonuna tıklandı (gelecekte profil ekranı açılacak).");
    }

    private void loadAllEvents() {
    eventCardContainer.getChildren().clear();

    try {
        List<QueryDocumentSnapshot> documents = FirestoreClient
                .getFirestore()
                .collection("events")
                .get()
                .get()
                .getDocuments();

        for (DocumentSnapshot doc : documents) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
            HBox card = loader.load();

            EventCardController controller = loader.getController();
            controller.setData(doc.getId(), doc.getData());

            eventCardContainer.getChildren().add(card);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}


    
}
