package com.project1;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import com.project1.EventDetailController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.Timestamp;

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

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.time.ZoneId;

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
   private String eventId;
    private Map<String,Object> eventData;
    private boolean isEditMode = false;

    private File selectedPosterFile;
    private String clubId;
    private String clubName;

    private UserModel loggedInUser;
    // If non-null, indicates we are editing an existing event
    private String editingEventId;

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

    /**
     * Pre-fills the form with an existing event’s data.
     * @param eventId  Firestore ID of the event to edit
     * @param data     Map of its fields
     */
    public void populateForEdit(String eventId, Map<String, Object> data) {
        this.eventId     = eventId;
        this.eventData   = data;
        this.isEditMode  = true;

        // 1) Text fields
        nameField.setText((String) data.get("name"));
        String type = (String) data.get("eventType");
        typeChoice.setValue(type.substring(0,1).toUpperCase() + type.substring(1));

        // 2) Date + time
        // Handle Firestore Timestamp or java.util.Date
        Object rawDate = data.get("eventDate");
        java.util.Date d;
        if (rawDate instanceof com.google.cloud.Timestamp ts) {
            d = ts.toDate();
        } else if (rawDate instanceof java.util.Date ud) {
            d = ud;
        } else {
            d = new java.util.Date(); // fallback to now
        }
        java.time.LocalDate ld = d.toInstant()
                                  .atZone(java.time.ZoneId.systemDefault())
                                  .toLocalDate();
        datePicker.setValue(ld);
        java.time.LocalTime lt = d.toInstant()
                                  .atZone(java.time.ZoneId.systemDefault())
                                  .toLocalTime();
        hourSpinner.getValueFactory().setValue(lt.getHour());
        minuteSpinner.getValueFactory().setValue(lt.getMinute());

        // 3) Location & participants
        locationField.setText((String) data.get("location"));
        minPartSpinner.getValueFactory()
            .setValue(((Number)data.get("minParticipants")).intValue());
        maxPartSpinner.getValueFactory()
            .setValue(((Number)data.get("maxParticipants")).intValue());

        // 4) Description & poster
        descriptionArea.setText((String) data.get("description"));
        String poster = (String) data.get("posterUrl");
        posterPathLabel.setText(poster != null ? poster : "(no file)");
        createButton.setText("Save Changes");
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
            if (isEditMode) {
                // Update existing event
                db.collection("events").document(eventId).set(data).get();
                db.collection("clubs")
                  .document(clubId)
                  .update("activeEventCount", FieldValue.increment(1))
                  .get();
                // Navigate back to updated event detail view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_detail.fxml"));
                Parent root = loader.load();
                EventDetailController detailCtrl = loader.getController();
                detailCtrl.setLoggedInUser(loggedInUser);
                detailCtrl.setEventData(eventId, data);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = stage.getScene();
                if (scene == null) {
                    scene = new Scene(root);
                    stage.setScene(scene);
                } else {
                    scene.setRoot(root);
                }
                return;
            }
            // for new event creation, keep existing logic
            else {
                // Add new event
                String newEventId = db.collection("events").add(data).get().getId();
                // increment the club's active event count
                db.collection("clubs")
                  .document(clubId)
                  .update("activeEventCount", FieldValue.increment(1))
                  .get();
                // After creation, fallback to profile as before
                SceneChanger.switchScene(event, "profile.fxml", controller -> {
                    if (controller instanceof ProfileController pc) {
                        pc.setUser(loggedInUser);
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Event oluşturulamadı: " + ex.getMessage(), ButtonType.OK)
                .showAndWait();
        }
    }


}
