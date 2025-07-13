package com.project1;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

/**
 * Controller class for the Club Profile view.
 * Displays detailed information about a selected club,
 * including name, logo, description, foundation date,
 * managers, and related events.
 * 
 * Associated with: club_profile.fxml
 * 
 * @author: Utku
 */
public class ClubProfileController {

    /** ImageView displaying the club's logo */
    @FXML
    private ImageView clubLogo;

    /** Label for club name */
    @FXML
    private Label clubNameLabel;

    /** Label for foundation date */
    @FXML
    private Label foundationDateLabel;

    /** TextArea displaying club's description */
    @FXML
    private TextArea descriptionArea;

    /** ListView displaying managers of the club */
    @FXML
    private ListView<String> managersListView;

    /** VBox container for the event cards (optional/legacy) */
    @FXML
    private VBox eventCardContainer;

    /** VBox container for club-specific events */
    @FXML
    private VBox eventListContainer;

    /** Club document ID from Firestore */
    private String clubId;

    /**
     * Sets the current club ID and loads its data from Firestore.
     * 
     * @param clubId Firestore document ID of the club
     */
    public void setClubId(String clubId) {
        this.clubId = clubId;
        loadClubData();     // Load club info (logo, desc, managers, etc.)
        loadClubEvents();   // Load events created by this club
    }

    /**
     * Loads the club's detailed data from Firestore and populates the UI.
     * This includes name, description, logo, managers, and (future) events.
     */
    private void loadClubData() {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentSnapshot doc = db.collection("clubs").document(clubId).get().get();

            if (doc.exists()) {
                Map<String, Object> data = doc.getData();

                // Set club name and foundation date
                clubNameLabel.setText((String) data.get("name"));
                foundationDateLabel.setText("Founded: " + data.get("foundationDate"));

                // Set club description
                descriptionArea.setText((String) data.get("description"));

                // Load and display logo
                String logoUrl = (String) data.get("logoUrl");
                if (logoUrl != null) {
                    clubLogo.setImage(new Image(logoUrl, true)); // async load
                }

                // Populate manager list
                List<String> managers = (List<String>) data.get("managers");
                managersListView.getItems().setAll(managers);

                // Clear old cards (if legacy system used)
                eventCardContainer.getChildren().clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads all events from Firestore, filters by this club's ID,
     * and displays each as an event card (via FXML).
     */
    private void loadClubEvents() {
        eventListContainer.getChildren().clear();

        try {
            List<QueryDocumentSnapshot> documents = FirestoreClient
                    .getFirestore()
                    .collection("events")
                    .get()
                    .get()
                    .getDocuments();

            for (QueryDocumentSnapshot doc : documents) {
                String eventClubId = doc.getString("clubId");

                if (eventClubId != null && eventClubId.equals(clubId)) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
                    HBox card = loader.load();

                    EventCardController controller = loader.getController();
                    controller.setData(doc.getId(), doc.getData());

                    eventListContainer.getChildren().add(card);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Back" button action to return to the admin dashboard.
     * 
     * @param event the event triggered by button click
     */
    @FXML
    private void handleBack(ActionEvent event) {
        SceneChanger.switchScene(event, "admin_dashboard.fxml");
    }
}
