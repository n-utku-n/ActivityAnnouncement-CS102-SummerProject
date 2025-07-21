package com.project1;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;

import java.util.Map;

/**
 * Controller for the admin club card UI.
 * Allows admins to view club details and delete clubs.
 * Automatically refreshes the dashboard after deletion.
 * 
 * @author Utku
 */
public class ClubCardAdminController {

    @FXML
    private Label clubNameLabel;

    @FXML
    private Label eventCountLabel;

    @FXML
    private Label managerCountLabel;

    @FXML
    private Label participantCountLabel;

    @FXML
    private ImageView clubLogo;

    @FXML
    private Button viewButton;

    @FXML
    private Button deleteButton;

    private String clubId;
    private String clubName;

    private AdminDashboardController dashboardController;

    /**
     * Sets the parent dashboard controller to allow refreshing after actions.
     * 
     * @param controller the AdminDashboardController instance
     */
    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
    }

    /**
 * Populates the card UI with club data from Firestore.
 *
 * @param id   the club document ID
 * @param data the club document data as a map
 */
public void setData(String id, Map<String, Object> data) {
    this.clubId = id;
    this.clubName = (String) data.get("name");

    clubNameLabel.setText(clubName);
    eventCountLabel.setText("Events: " + data.getOrDefault("eventCount", 0));
    managerCountLabel.setText("Managers: " + data.getOrDefault("managerCount", 0));
    participantCountLabel.setText("Participants: " + data.getOrDefault("participantCount", 0));

    String logoUrl = (String) data.get("logoUrl");

    System.out.println("🎯 clubLogo objesi null mı? " + (clubLogo == null));
    System.out.println("🎯 logoUrl: " + logoUrl);

    if (logoUrl != null && !logoUrl.isEmpty() && !logoUrl.contains("[") && !logoUrl.contains("]")) {
        try {
            Image image = new Image(logoUrl, true);
            clubLogo.setImage(image);

            System.out.println("🎯 image.isError(): " + image.isError());
            System.out.println("🎯 clubLogo.getImage(): " + (clubLogo.getImage() != null));
        } catch (Exception e) {
            System.out.println("⚠️ Logo yüklenemedi (URL hatası): " + logoUrl);
            e.printStackTrace();
        }
    } else {
        System.out.println("⚠️ Boş veya geçersiz logoUrl bulundu: " + logoUrl);
    }
}




    /**
     * Handles the "View" button click to navigate to the club profile screen.
     * 
     * @param event the button click event
     */
    @FXML
    private void handleView(ActionEvent event) {
        SceneChanger.switchScene(event, "club_profile.fxml");
        // You may later pass clubId to ClubProfileController if needed
    }

    /**
     * Handles the "Delete" button click with a confirmation dialog.
     * Deletes the club from Firestore and refreshes the club list.
     */
   @FXML
private void handleDelete() {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Delete Club");
    confirm.setHeaderText("Are you sure you want to delete this club?");
    confirm.setContentText("This action cannot be undone.");

    confirm.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            Firestore db = FirestoreClient.getFirestore();
            ApiFuture<WriteResult> future = db.collection("clubs").document(clubId).delete();

            try {
                future.get(); // Wait for deletion
                System.out.println("✅ Club deleted: " + clubId);

                if (parentController != null) {
                    parentController.refreshClubList();
                }

            } catch (Exception e) {
                System.err.println("❌ Failed to delete club: " + e.getMessage());
                e.printStackTrace();
            }
        }
    });
}



        private AdminDashboardController parentController;

        public void setParentController(AdminDashboardController controller) {
            this.parentController = controller;
        }
}
