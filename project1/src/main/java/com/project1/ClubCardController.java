package com.project1;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.DocumentSnapshot;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.Map;
import java.util.List;

import com.project1.ClubProfileController;

import com.project1.SceneChanger;

/**
 * Controller class for representing a single club card UI component.
 * Displays club details such as name, logo, and manager count.
 * Provides options to view club profile or delete the club.
 * 
 * Associated with FXML: club_card.fxml
 * 
 * @author Utku, Serra
 */
public class ClubCardController {

    @FXML
    private HBox clubCard;

    @FXML
    private ImageView clubLogo;

    @FXML
    private Label clubNameLabel, eventCountLabel, participantCountLabel, managerCountLabel;

    @FXML
    private Button viewButton;

    private String clubId;
    private String clubName;

    /** Event ID to return to when opening club profile */
    private String previousEventId;
    private UserModel currentUser;

    // Store current user's UID for navigation
    private String currentUserUid;

    public void setCurrentUser(UserModel user) {
        this.currentUser = user;
        if (user != null) {
            this.currentUserUid = user.getUid();
        }
    }

    /**
     * Loads club data by ID and populates the card.
     */
    public void setClubInfo(String clubId, String clubName) {
        this.clubId = clubId;
        this.clubName = clubName;
        // Asynchronously fetch club document
        CompletableFuture.runAsync(() -> {
            try {
                DocumentSnapshot doc = FirestoreClient.getFirestore()
                    .collection("clubs")
                    .document(clubId)
                    .get().get();
                if (doc.exists()) {
                    Map<String, Object> data = doc.getData();
                    Platform.runLater(() -> setData(clubId, data));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    /**
     * Populates the card UI with the given club data.
     *
     * @param id   the document ID of the club
     * @param data the data map retrieved from Firestore
     */
    public void setData(String id, Map<String, Object> data) {
        this.clubId = id;
        this.clubName = (String) data.get("name");

        clubNameLabel.setText(clubName);
        // Set active event count from data map
        Object activeObj = data.get("activeEventCount");
        int activeCount = activeObj instanceof Number ? ((Number) activeObj).intValue() : 0;
        eventCountLabel.setText("Active Events: " + activeCount);
        participantCountLabel.setText("Participants: 0"); // Gelecekte güncellenecek
        // Fetch manager name by UID list
        Object mgrObj = data.get("managers");
        if (mgrObj instanceof List<?>) {
            List<String> mgrList = (List<String>) mgrObj;
            if (!mgrList.isEmpty()) {
                String mgrId = mgrList.get(0);
                // asynchronously load user info
                CompletableFuture.runAsync(() -> {
                    try {
                        DocumentSnapshot userDoc = FirestoreClient.getFirestore()
                            .collection("users")
                            .document(mgrId)
                            .get().get();
                        String fullName = userDoc.getString("name") + " " + userDoc.getString("surname");
                        Platform.runLater(() -> managerCountLabel.setText("Manager: " + fullName));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> managerCountLabel.setText("Manager: Unknown"));
                    }
                });
            } else {
                managerCountLabel.setText("Manager: N/A");
            }
        } else {
            managerCountLabel.setText("Manager: N/A");
        }

        try {
            String logoUrl = (String) data.get("logoUrl");
            if (logoUrl != null && !logoUrl.isEmpty()) {
                clubLogo.setImage(new Image(logoUrl, true));
            } else {
                clubLogo.setImage(null);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Invalid logo URL for club: " + clubName);
            clubLogo.setImage(null);
        }
    }

    /**
     * Stores the previous event ID so ClubProfile can return here.
     */
    public void setPreviousEventId(String eventId) {
        this.previousEventId = eventId;
    }

    /**
     * Handles the "View" button action.
     * Switches to the club profile scene and passes the selected club's ID.
     *
     * @param event the action event from the view button
     */
    @FXML
    private void handleView(ActionEvent event) {
        SceneChanger.switchScene(event, "club_profile.fxml", controller -> {
            if (controller instanceof ClubProfileController cpc) {
                cpc.setCurrentUser(currentUser);
                cpc.setClubContext(clubId, previousEventId);
            }
        });
    }

    
}
