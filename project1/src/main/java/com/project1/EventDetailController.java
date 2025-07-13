package com.project1;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Controller class for displaying detailed information about an event.
 * Includes event name, description, rules, participant counts, image, and related club info.
 *
 * @author Utku
 */
public class EventDetailController {

    /** Label displaying the name of the event */
    @FXML
    private Label eventNameLabel;

    /** Label showing current, min and max participants */
    @FXML
    private Label participantInfoLabel;

    /** ImageView for the event's poster */
    @FXML
    private ImageView eventImage;

    /** Text area showing event description */
    @FXML
    private TextArea descriptionArea;

    /** Text area displaying event rules */
    @FXML
    private TextArea rulesArea;

    /** Placeholder to insert the club card dynamically */
    @FXML
    private VBox clubCardPlaceholder;

    /** ID of the organizing club */
    private String clubId;

    /**
     * Populates the event detail view with data from Firestore.
     * Loads event metadata, poster, and corresponding club card.
     *
     * @param eventId Firestore document ID of the event
     * @param data Event details as key-value pairs
     */
    public void setEventData(String eventId, Map<String, Object> data) {
        try {
            // Basic text fields
            eventNameLabel.setText((String) data.get("name"));
            descriptionArea.setText((String) data.get("description"));
            rulesArea.setText((String) data.get("rules"));

            // Participant info formatting
            int min = (int) data.get("minParticipants");
            int max = (int) data.get("maxParticipants");
            int current = (int) data.get("currentParticipants");
            participantInfoLabel.setText("👥 Participants: " + current + " (Min: " + min + ", Max: " + max + ")");

            // Load event poster if available
            String posterUrl = (String) data.get("posterUrl");
            if (posterUrl != null && !posterUrl.isEmpty()) {
                try {
                    eventImage.setImage(new Image(posterUrl, true));
                } catch (Exception e) {
                    System.out.println("⚠️ Invalid poster URL: " + posterUrl);
                    e.printStackTrace();
                }
            }

            // Load associated club card
            clubId = (String) data.get("clubId");
            DocumentSnapshot clubDoc = FirestoreClient.getFirestore()
                    .collection("clubs")
                    .document(clubId)
                    .get()
                    .get();

            if (clubDoc.exists()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/club_card.fxml"));
                Parent clubCard = loader.load();

                ClubCardController controller = loader.getController();
                controller.setData(clubDoc.getId(), clubDoc.getData());

                clubCardPlaceholder.getChildren().add(clubCard);
            }
            
            else {
                Label fallback = new Label("❌ Club information could not be loaded.");
                fallback.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                clubCardPlaceholder.getChildren().add(fallback);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates back to the main dashboard screen when the back button is clicked.
     * Can be changed to redirect to a different dashboard depending on user role.
     *
     * @param event The button click event
     */
    @FXML
    private void handleBack(ActionEvent event) {
        SceneChanger.switchScene(event, "main_dashboard.fxml"); // Can be replaced with admin_dashboard.fxml if needed
    }
}
