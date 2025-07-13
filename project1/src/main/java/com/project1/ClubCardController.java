package com.project1;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.util.Map;

/**
 * Controller class for representing a single club card UI component.
 * Displays club details such as name, logo, and manager count.
 * Provides options to view club profile or delete the club.
 * 
 * Associated with FXML: club_card.fxml
 * 
 * @author Utku
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
        eventCountLabel.setText("Active Events: 0"); // Gelecekte güncellenecek
        participantCountLabel.setText("Participants: 0"); // Gelecekte güncellenecek
        managerCountLabel.setText("Managers: " + ((java.util.List<?>) data.get("managers")).size());

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
     * Handles the "View" button action.
     * Switches to the club profile scene and passes the selected club's ID.
     *
     * @param event the action event from the view button
     */
    @FXML
    private void handleView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/club_profile.fxml"));
            Parent root = loader.load();

            ClubProfileController controller = loader.getController();
            controller.setClubId(clubId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}
