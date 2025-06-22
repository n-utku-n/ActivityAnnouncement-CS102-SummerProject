package com.project1;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Controller for the event card UI component.
 * Displays brief information about an event and allows users
 * to navigate to its detail page.
 *
 * @author Utku
 */
public class EventCardController {

    /** Main card container */
    @FXML
    private VBox eventCard;

    /** ImageView showing the event poster */
    @FXML
    private ImageView eventImage;

    /** ImageView showing the organizing club's logo */
    @FXML
    private ImageView clubLogo;

    /** Label showing the event name */
    @FXML
    private Label eventNameLabel;

    /** Label for minimum number of participants */
    @FXML
    private Label minParticipantsLabel;

    /** Label for maximum number of participants */
    @FXML
    private Label maxParticipantsLabel;

    /** Label for current number of participants */
    @FXML
    private Label currentParticipantsLabel;

    /** Button to navigate to detailed event view */
    @FXML
    private Button detailsButton;

    /** Firestore event document ID */
    private String eventId;

    /** Firestore club document ID */
    private String clubId;

    /** Cached event data for later use */
    private Map<String, Object> eventData;

    /**
     * Populates the event card with data retrieved from Firestore.
     *
     * @param eventId Firestore document ID of the event
     * @param data Map containing event fields like name, participants, image URLs, etc.
     */
    public void setData(String eventId, Map<String, Object> data) {
        this.eventId = eventId;
        this.eventData = data;

        // Set textual fields
        eventNameLabel.setText((String) data.get("name"));
        minParticipantsLabel.setText("Min: " + data.get("minParticipants"));
        maxParticipantsLabel.setText("Max: " + data.get("maxParticipants"));
        currentParticipantsLabel.setText("Current: " + data.get("currentParticipants"));

        // Set event poster image
        String eventImageUrl = (String) data.get("posterUrl");
        if (eventImageUrl != null && !eventImageUrl.isEmpty()) {
            eventImage.setImage(new Image(eventImageUrl, true));
        }

        // Set club logo (fetching from clubs collection)
        this.clubId = (String) data.get("clubId");
        try {
            DocumentSnapshot clubDoc = FirestoreClient.getFirestore()
                .collection("clubs")
                .document(clubId)
                .get()
                .get();

            if (clubDoc.exists()) {
                String clubLogoUrl = (String) clubDoc.get("logoUrl");
                if (clubLogoUrl != null && !clubLogoUrl.isEmpty()) {
                    clubLogo.setImage(new Image(clubLogoUrl, true));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the detailed event screen when the "Details" button is clicked.
     *
     * @param event The ActionEvent triggered by the button click
     */
    @FXML
    private void handleDetails(ActionEvent event) {
        try {
            // Load event_detail.fxml and pass event data to controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_detail.fxml"));
            Parent root = loader.load();

            EventDetailController controller = loader.getController();
            controller.setEventData(eventId, eventData);

            // Switch scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
