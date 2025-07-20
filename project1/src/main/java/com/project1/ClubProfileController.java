package com.project1;

import java.util.ArrayList;
import java.util.List;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import com.project1.EventDetailController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.scene.layout.FlowPane;
import com.google.cloud.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import com.project1.ProfileController;
import com.project1.SceneChanger;

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

    @FXML private Label participantCountLabel;
    @FXML private Button followButton;

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


    private UserModel currentUser;
    private String currentUserUid;

    public void setCurrentUser(UserModel user) {
        this.currentUser = user;
        if (user != null) {
            this.currentUserUid = user.getUid();
        }
    }

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
                // Parse foundationDate stored as Timestamp and format
                com.google.cloud.Timestamp foundationTs = doc.getTimestamp("foundationDate");
                if (foundationTs != null) {
                    LocalDate foundationDate = foundationTs.toDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                    String formatted = foundationDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
                    foundationDateLabel.setText("Founded: " + formatted);
                } else {
                    foundationDateLabel.setText("Founded: N/A");
                }

                // Set club description
                descriptionLabel.setText((String) data.get("description"));

                // Load managers from the club document's managers field
                @SuppressWarnings("unchecked")
                List<String> managerIds = (List<String>) doc.get("managers");
                managersContainer.getChildren().clear();
                if (managerIds == null || managerIds.isEmpty()) {
                    Label lbl = new Label("Unknown Manager");
                    lbl.getStyleClass().add("manager-label");
                    managersContainer.getChildren().add(lbl);
                } else {
                    for (String managerId : managerIds) {
                        DocumentSnapshot mDoc = db.collection("users")
                            .document(managerId)
                            .get().get();
                        String first = mDoc.getString("name");
                        String last = mDoc.getString("surname");
                        String managerName = ((first != null) ? first : "") + " " + ((last != null) ? last : "");
                        if (managerName.trim().isEmpty()) {
                            managerName = "Unknown Manager";
                        }
                        Label lbl = new Label(managerName);
                        lbl.getStyleClass().add("manager-label");
                        managersContainer.getChildren().add(lbl);
                    }
                }

                // Load and display logo
                String logoUrl = (String) data.get("logoUrl");
                if (logoUrl != null) {
                    clubLogo.setImage(new Image(logoUrl, true)); // async load
                }

                // Clear old cards (if legacy system used)
                // Populate follower count and button
                @SuppressWarnings("unchecked")
                List<String> followerIds = (List<String>) data.get("participants");
                int followerCount = followerIds != null ? followerIds.size() : 0;
                participantCountLabel.setText("Followers: " + followerCount);
                boolean isFollowing = followerIds != null && followerIds.contains(currentUserUid);
                followButton.setText(isFollowing ? "Unfollow" : "Follow");
                followButton.getStyleClass().removeAll("follow-button","unfollow-button");
                followButton.getStyleClass().add(isFollowing ? "unfollow-button" : "follow-button");
                followButton.setVisible(true);
                followButton.setManaged(true);

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
                        clubCtrl.setCurrentUser(currentUser);
                        clubCtrl.setData(doc.getId(), doc.getData());
                        eventListContainer.getChildren().add(clubCard);

                        // Add only upcoming (not yet started) events to Active Events
                        Timestamp eventTs = doc.getTimestamp("eventDate");
                        if (eventTs != null && eventTs.toDate().after(new Date())) {
                            FXMLLoader loaderActive = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
                            VBox activeCard = loaderActive.load();
                            EventCardController activeCtrl = loaderActive.getController();
                            activeCtrl.setCurrentUser(currentUser);
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
     * Handles the "Back" button action to return either to Event Detail or Profile.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        if (previousEventId != null && !previousEventId.trim().isEmpty()) {
            // Return to Event Detail
            SceneChanger.switchScene(event, "event_detail.fxml", controller -> {
                if (controller instanceof EventDetailController edc) {
                    edc.setLoggedInUser(currentUser);
                    edc.setEventId(previousEventId);
                }
            });
        } else {
            // Return to Profile
            SceneChanger.switchScene(event, "profile.fxml", controller -> {
                if (controller instanceof ProfileController pc) {
                    pc.setUser(currentUser);
                }
            });
        }
    }

    @FXML
    private void handleFollow(ActionEvent evt) {
      try {
        DocumentReference clubRef = FirestoreClient
          .getFirestore()
          .collection("clubs")
          .document(clubId);
        if ("Follow".equals(followButton.getText())) {
          clubRef.update("participants", FieldValue.arrayUnion(currentUserUid)).get();
        } else {
          clubRef.update("participants", FieldValue.arrayRemove(currentUserUid)).get();
        }
        loadClubData();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
}
