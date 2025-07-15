package com.project1;

import java.util.ArrayList;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import com.project1.EventDetailController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.layout.FlowPane;
import com.google.cloud.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import com.project1.EventDetailController;
import java.io.IOException;

/**
 * Controller class for the Club Profile view.
 * Displays detailed information about a selected club,
 * including name, logo, description, foundation date,
 * managers, and related events.
 * 
 * Associated with: club_profile.fxml
 * 
 * @author: Utku Serra
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

    /** Label displaying club's description */
    @FXML
    private Label descriptionLabel;

    /** VBox container for managers of the club */
    @FXML
    private VBox managersContainer;

    /** VBox container for the event cards (optional/legacy) */
    @FXML
    private FlowPane eventCardContainer;

    /** VBox container for club-specific events */
    @FXML
    private FlowPane eventListContainer;

    /** Club document ID from Firestore */
    private String clubId;

    /** Event ID to return to when clicking Back */
    private String previousEventId;

    @FXML
    public void initialize() {
        clubLogo.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs2, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        ((Stage) newWindow).setMaximized(true);
                    }
                });
            }
        });
    }

    /**
     * Initializes this view for a given club and event.
     * @param clubId Firestore ID of the club
     * @param previousEventId Event ID to return to on back
     */
    public void setClubContext(String clubId, String previousEventId) {
        this.clubId = clubId;
        this.previousEventId = previousEventId;
        try {
            loadClubData();    // populate descriptionArea and managersListView
            loadClubEvents();  // populate event sections
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @deprecated Use setClubContext instead.
     */
    @Deprecated
    public void setClubId(String clubId) {
        setClubContext(clubId, null);
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
                descriptionLabel.setText((String) data.get("description"));

                // Load managers for this club from users collection
                try {
                    Query mgrQuery = db.collection("users")
                        .whereEqualTo("clubId", clubId)
                        .whereEqualTo("role", "manager");
                    List<QueryDocumentSnapshot> mgrDocs = mgrQuery.get().get().getDocuments();
                    List<String> managerNames = new ArrayList<>();
                    for (QueryDocumentSnapshot mDoc : mgrDocs) {
                        String name = mDoc.getString("name");
                        if (name != null) managerNames.add(name);
                    }
                    // If no managers found, show default
                    if (managerNames.isEmpty()) {
                        managerNames.add("Unknown Manager");
                    }
                    // Populate manager labels
                    managersContainer.getChildren().clear();
                    for (String name : managerNames) {
                        Label lbl = new Label(name);
                        lbl.getStyleClass().add("manager-label");
                        managersContainer.getChildren().add(lbl);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Load and display logo
                String logoUrl = (String) data.get("logoUrl");
                if (logoUrl != null) {
                    clubLogo.setImage(new Image(logoUrl, true)); // async load
                }

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
        eventCardContainer.getChildren().clear();
        eventListContainer.getChildren().clear();

        try {
            List<QueryDocumentSnapshot> documents = FirestoreClient
                    .getFirestore()
                    .collection("events")
                    .get()
                    .get()
                    .getDocuments();

            for (QueryDocumentSnapshot doc : documents) {
                try {
                    String eventClubId = doc.getString("clubId");

                    if (eventClubId != null && eventClubId.equals(clubId)) {
                        // Always add to Club Events list
                        FXMLLoader loaderClub = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
                        VBox clubCard = loaderClub.load();
                        EventCardController clubCtrl = loaderClub.getController();
                        clubCtrl.setData(doc.getId(), doc.getData());
                        eventListContainer.getChildren().add(clubCard);

                        // If not expired, also add to Active Events
                        Timestamp endTs = doc.getTimestamp("endDate");
                        if (endTs == null || endTs.toDate().after(new Date())) {
                            FXMLLoader loaderActive = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
                            VBox activeCard = loaderActive.load();
                            EventCardController activeCtrl = loaderActive.getController();
                            activeCtrl.setData(doc.getId(), doc.getData());
                            eventCardContainer.getChildren().add(activeCard);
                        }
                    }
                } catch (Exception e) {
                    // Log and continue with next document
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Back" button action to always return to the Event Detail screen.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_detail.fxml"));
            Parent detailRoot = loader.load();
            EventDetailController detailCtrl = loader.getController();
            // Pass the previous event ID if available
            if (previousEventId != null && !previousEventId.trim().isEmpty()) {
                detailCtrl.setEventId(previousEventId);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(detailRoot));
            stage.setMaximized(true); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
