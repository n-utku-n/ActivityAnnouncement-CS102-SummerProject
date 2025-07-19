package com.project1;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FieldValue;
import com.google.firebase.cloud.FirestoreClient;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Collections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.project1.UserModel;

import com.google.firebase.cloud.StorageClient;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class CreateEventController implements Initializable {
    @FXML private TextField nameField;
    @FXML private ChoiceBox<String> typeChoice;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private TextField locationField;
    @FXML private Spinner<Integer> minPartSpinner;
    @FXML private Spinner<Integer> maxPartSpinner;
    @FXML private TextArea descriptionArea;
    @FXML private Label posterPathLabel;
    @FXML private Button createButton;

    private File selectedPosterFile;
    private String clubId;
    private String clubName;

    private UserModel loggedInUser;

        /**
     * @return ProfileController’dan gelen kulüp ID’si
     */
    public String getClubId() {
        return this.clubId;
    }

    /**
     * @return ProfileController’dan gelen kulüp adı
     */
    public String getClubName() {
        return this.clubName;
    }

    /** ProfileController’dan gelen kulüp bilgilerini ayarlar */
    public void setClubInfo(String clubId, String clubName) {
        this.clubId = clubId;
        this.clubName = clubName;
    }

    /** Called by previous controller to pass in the current user */
    public void setUser(UserModel user) {
        this.loggedInUser = user;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Event tipleri
        typeChoice.getItems().addAll("Workshop", "Seminar", "Hackathon", "Meeting", "Other");
        typeChoice.getSelectionModel().selectFirst();

        // Saat/Dakika spinners
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        // Katılımcı sayısı spinners
        minPartSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 20));
        maxPartSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 100));

        posterPathLabel.setText("(no file)");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneChanger.switchScene(event, "profile.fxml", controller -> {
            if (controller instanceof ProfileController pc) {
                pc.setUser(loggedInUser);
            }
        });
    }

    @FXML
    private void handleChooseImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            selectedPosterFile = file;
            posterPathLabel.setText(file.getName());
        }
    }

    @FXML
    private void handleCreateEvent(ActionEvent event) {
        try {
            String name = nameField.getText();
            String type = typeChoice.getValue().toLowerCase();
            LocalDate date = datePicker.getValue();
            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();
            Date eventDate = Date.from(
                LocalDateTime.of(date, LocalTime.of(hour, minute))
                    .atZone(ZoneId.systemDefault()).toInstant()
            );
            String location = locationField.getText();
            int minP = minPartSpinner.getValue();
            int maxP = maxPartSpinner.getValue();
            String description = descriptionArea.getText();
            String posterUrl = "";
            if (selectedPosterFile != null) {
                // upload to Firebase Storage
                String storageFileName = "posters/" + System.currentTimeMillis() + "_" + selectedPosterFile.getName();
                try (FileInputStream fis = new FileInputStream(selectedPosterFile)) {
                    String contentType = Files.probeContentType(selectedPosterFile.toPath());
                    StorageClient.getInstance()
                        .bucket()
                        .create(storageFileName, fis, contentType);
                    String bucketName = StorageClient.getInstance().bucket().getName();
                    String encodedPath = URLEncoder.encode(storageFileName, StandardCharsets.UTF_8.toString());
                    posterUrl = String.format(
                        "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                        bucketName, encodedPath
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Poster yüklenemedi: " + e.getMessage(), ButtonType.OK).showAndWait();
                    return;
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("eventType", type);
            data.put("eventDate", eventDate);
            data.put("location", location);
            data.put("minParticipants", minP);
            data.put("maxParticipants", maxP);
            data.put("currentParticipants", 0);
            data.put("participants", Collections.emptyList());
            data.put("averageRating", 0);
            data.put("ratingCount", 0);
            data.put("ratingSum", 0);
            data.put("clubId", clubId);
            data.put("clubName", clubName);
            data.put("description", description);
            data.put("timestamp", FieldValue.serverTimestamp());
            data.put("posterUrl", posterUrl);

            Firestore db = FirestoreClient.getFirestore();
            db.collection("events").add(data).get();

            SceneChanger.switchScene(event, "profile.fxml", controller -> {
                if (controller instanceof ProfileController pc) {
                    pc.setUser(loggedInUser);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Event oluşturulamadı: " + ex.getMessage(), ButtonType.OK)
                .showAndWait();
        }
    }


}
