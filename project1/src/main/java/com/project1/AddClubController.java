package com.project1;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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
    private String logoUrl;

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
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp")
    );
    selectedLogoFile = fileChooser.showOpenDialog(new Stage());

    if (selectedLogoFile != null) {
        logoFileNameLabel.setText(selectedLogoFile.getName());

        try {
            // Dosya uzantısından MIME tipi belirle
            String fileNameLower = selectedLogoFile.getName().toLowerCase();
            String contentType;
            if (fileNameLower.endsWith(".png")) contentType = "image/png";
            else if (fileNameLower.endsWith(".jpg") || fileNameLower.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (fileNameLower.endsWith(".webp")) contentType = "image/webp";
            else {
                showAlert(Alert.AlertType.ERROR, "Unsupported image format.");
                return;
            }

            // Logo dosyasını Firebase'e yükle
            String logoFileName = "logos/" + UUID.randomUUID() + selectedLogoFile.getName().substring(selectedLogoFile.getName().lastIndexOf('.'));
            String downloadToken = UUID.randomUUID().toString();
            String bucketName = StorageClient.getInstance().bucket().getName();

            BlobId blobId = BlobId.of(bucketName, logoFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .setMetadata(Map.of("firebaseStorageDownloadTokens", downloadToken))
                    .build();

            try (InputStream logoStream = new FileInputStream(selectedLogoFile)) {
                StorageClient.getInstance().bucket().getStorage().create(blobInfo, logoStream);
            }

            // Public URL oluştur
            logoUrl = "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/"
                    + logoFileName.replace("/", "%2F")
                    + "?alt=media&token=" + downloadToken;

            System.out.println("✅ Logo uploaded to Firebase: " + logoUrl);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Failed to upload logo: " + e.getMessage());
        }

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
        if (name.isEmpty() || description.isEmpty() || foundationDate == null || selectedLogoFile == null || logoUrl == null) {
            showAlert(Alert.AlertType.ERROR, "Please fill all fields and select a logo.");
            return;
        }

        // Create club data map
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> clubData = new HashMap<>();
        clubData.put("name", name);
        clubData.put("description", description);
        clubData.put("foundationDate", foundationDate.toString());
        clubData.put("logoUrl", logoUrl);  // logoUrl artık token içeren geçerli URL
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
