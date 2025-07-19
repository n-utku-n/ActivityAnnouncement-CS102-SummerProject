package com.project1;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.Map;

import com.project1.ClubProfileController;

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

    public void setCurrentUser(UserModel user) {
        this.currentUser = user;
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
        // Set manager name from nested map
        String managerName = "Unknown Manager";
        Object mgrObj = data.get("managers");
        if (mgrObj instanceof Map) {
            Map<?,?> mgrMap = (Map<?,?>) mgrObj;
            Object nameField = mgrMap.get("name");
            if (nameField instanceof String) {
                managerName = (String) nameField;
            }
        }
        managerCountLabel.setText("Manager: " + managerName);

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
        FXMLLoader loader = SceneChanger.switchScene(event, "club_profile.fxml");
        if (loader != null) {
            ClubProfileController controller = loader.getController();
            controller.setClubContext(clubId, previousEventId);
            controller.setCurrentUser(currentUser); 
        }
    }

    
}
