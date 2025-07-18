package com.project1;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Controller class for the admin view of event details.
 * Allows viewing and deleting an event, including club information and participant stats.
 * 
 * Associated FXML: event_detail_admin.fxml
 * 
 * @author Utku
 */
public class EventDetailAdminController {

    @FXML
    private Label eventNameLabel;

    @FXML
    private ImageView eventImage;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextArea rulesArea;

    @FXML
    private Label participantInfoLabel;

    @FXML
    private VBox clubCardPlaceholder;

    private String eventId;
    private String clubId;
    

    /**
     * Sets the event data and populates the UI fields accordingly.
     * Also loads the related club card.
     *
     * @param eventId Firestore document ID of the event
     * @param data Event fields from Firestore
     */
    public void setEventData(String eventId, Map<String, Object> data) {
        this.eventId = eventId;

        // Set event basic info
        eventNameLabel.setText((String) data.get("name"));
        descriptionArea.setText((String) data.get("description"));
        rulesArea.setText((String) data.get("rules"));

        // Set participants info
        int min = (int) data.get("minParticipants");
        int max = (int) data.get("maxParticipants");
        int current = (int) data.get("currentParticipants");
        participantInfoLabel.setText("👥 Participants: " + current + " (Min: " + min + ", Max: " + max + ")");

        // Set image if available
        String posterUrl = (String) data.get("posterUrl");
        if (posterUrl != null && !posterUrl.isEmpty()) {
            try {
                eventImage.setImage(new Image(posterUrl, true));
            } catch (Exception e) {
                System.out.println("⚠️ Invalid poster URL: " + posterUrl);
            }
        }

        // Load club info
        clubId = (String) data.get("clubId");
        loadClubCard();
    }

    /**
     * Loads the associated club card and adds it to the placeholder VBox.
     */
    private void loadClubCard() {
        try {
            DocumentSnapshot clubDoc = FirestoreClient.getFirestore()
                    .collection("clubs")
                    .document(clubId)
                    .get()
                    .get();

            if (clubDoc.exists()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/club_card_admin.fxml"));
                Parent clubCard = loader.load();

                ClubCardAdminController controller = loader.getController();
                controller.setData(clubDoc.getId(), clubDoc.getData());

                clubCardPlaceholder.getChildren().add(clubCard);
            } else {
                Label fallback = new Label("❌ Club info not found.");
                clubCardPlaceholder.getChildren().add(fallback);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns to the admin dashboard screen.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        SceneChanger.switchScene(event, "admin_dashboard.fxml");
    }

    /**
     * Deletes the event document from Firestore and returns to admin dashboard.
     */
    @FXML
    private void handleDeleteEvent(ActionEvent event) {
        try {
            FirestoreClient.getFirestore()
                    .collection("events")
                    .document(eventId)
                    .delete();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Event Deleted");
            alert.setHeaderText(null);
            alert.setContentText("The event has been successfully deleted.");
            alert.showAndWait();

            SceneChanger.switchScene(event, "admin_dashboard.fxml");

        } catch (Exception e) {
            e.printStackTrace();

            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("Event deletion failed");
            error.setContentText("An error occurred while deleting the event.");
            error.showAndWait();
        }
    }
}
