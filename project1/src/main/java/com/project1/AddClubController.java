package com.project1;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for handling the creation of a new club by an admin user.
 * Allows the user to input club details, upload a logo to Firebase Storage,
 * and save the club data in Firestore.
 * 
 * FXML is linked to 'add_club.fxml'.
 * 
 * @author Utku
 */
public class AddClubController {

    /** TextField for entering the club's name */
    @FXML
    private TextField clubNameField;

    /** TextArea for entering the club's description */
    @FXML
    private TextArea descriptionArea;

    /** DatePicker to select the club's foundation date */
    @FXML
    private DatePicker foundationDatePicker;

    /** Label to display the selected logo file name */
    @FXML
    private Label logoFileNameLabel;

    /** Holds the selected logo file from user's system */
    private File selectedLogoFile;

    /**
     * Handles logo file selection using a FileChooser.
     * Only PNG files are allowed. Sets the file name in the label.
     *
     * @param event the ActionEvent triggered by the 'Select Logo' button.
     */
    @FXML
    private void handleSelectLogo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Club Logo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        selectedLogoFile = fileChooser.showOpenDialog(new Stage());

        if (selectedLogoFile != null) {
            logoFileNameLabel.setText(selectedLogoFile.getName());
        } else {
            logoFileNameLabel.setText("No file selected");
        }
    }

    /**
     * Switches back to the admin dashboard scene.
     *
     * @param event the ActionEvent triggered by the 'Back' button.
     */
    @FXML
    private void handleBackButton(ActionEvent event) {
        SceneChanger.switchScene(event, "admin_dashboard.fxml");
    }

    /**
     * Handles the creation of a new club.
     * Validates inputs, uploads the logo to Firebase Storage,
     * and saves the club data in Firestore.
     *
     * @param event the ActionEvent triggered by the 'Create Club' button.
     */
   @FXML
private void handleCreateClub(ActionEvent event) {
    try {
        String name = clubNameField.getText();
        String description = descriptionArea.getText();
        LocalDate foundationDate = foundationDatePicker.getValue();

        // Validation: Check if all fields are filled
        if (name.isEmpty() || description.isEmpty() || foundationDate == null || selectedLogoFile == null) {
            showAlert(Alert.AlertType.ERROR, "Please fill all fields and select a logo.");
            return;
        }

        // Upload logo to Firebase Storage
        String logoFileName = "logos/" + UUID.randomUUID() + ".png";
        try (InputStream logoStream = new FileInputStream(selectedLogoFile)) {
            StorageClient.getInstance().bucket().create(logoFileName, logoStream, "image/png");
        }

        // Optional: Confirm the file was uploaded
        if (StorageClient.getInstance().bucket().get(logoFileName) == null) {
            showAlert(Alert.AlertType.ERROR, "Logo upload failed, please try again.");
            return;
        }

        // Generate public URL for the uploaded logo
       String logoUrl = "https://firebasestorage.googleapis.com/v0/b/project1-9c22f.appspot.com/o/"
        + logoFileName.replace("/", "%2F")
        + "?alt=media";

        System.out.println("✅ Logo URL: " + logoUrl);

        // Create club data map
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> clubData = new HashMap<>();
        clubData.put("name", name);
        clubData.put("description", description);
        clubData.put("foundationDate", foundationDate.toString());
        clubData.put("logoUrl", logoUrl);
        clubData.put("managers", new java.util.ArrayList<>());

        // Add to Firestore
        ApiFuture<DocumentReference> result = db.collection("clubs").add(clubData);
        result.get(); // Wait until write completes

        showAlert(Alert.AlertType.INFORMATION, "✅ Club created successfully!");
        SceneChanger.switchScene(event, "admin_dashboard.fxml");

    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "❌ Failed to create club: " + e.getMessage());
    }
    }

    /**
     * Utility method to show alerts to the user.
     *
     * @param type the type of alert (ERROR, INFORMATION, etc.)
     * @param msg  the message to display
     */
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle("Club Creation");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
