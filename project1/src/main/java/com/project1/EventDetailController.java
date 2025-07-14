package com.project1;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.shape.Rectangle;
import javafx.beans.value.ChangeListener;

import java.util.Map;

/**
 * Controller class for displaying detailed information about an event.
 */
public class EventDetailController {

    @FXML private Label eventNameLabel;
    @FXML private Label participantInfoLabel;
    @FXML private ImageView eventImage;
    @FXML private Label descriptionLabel;
    @FXML private Label rulesLabel;
    @FXML private VBox clubCardPlaceholder;

    @FXML private ProgressBar participantBar;
    @FXML private Rectangle minParticipantLine;
    private ChangeListener<Number> widthListener;

    private String clubId;

    /**
     * Populates the event detail view with data from Firestore.
     */
    public void setEventData(String eventId, Map<String, Object> data) {
        try {
            // 1) Başlık & metinler
            eventNameLabel.setText(getString(data, "name", "Unnamed Event"));
            descriptionLabel.setText(getString(data, "description", ""));
            rulesLabel.setText(getString(data, "rules", ""));

            // 2) Katılımcı bilgisi
            int current = getNumber(data, "currentParticipants", 0);
            int min     = getNumber(data, "minParticipants", 0);
            int max     = getNumber(data, "maxParticipants", 0);
            participantInfoLabel.setText(
                String.format("👥 Participants: %d (Min: %d, Max: %d)", current, min, max)
            );

            // setup progress bar
            double progress = max > 0 ? (double) current / max : 0;
            participantBar.setProgress(progress);

            // add listener to position red marker
            if (widthListener != null) {
                participantBar.widthProperty().removeListener(widthListener);
            }
            final double ratio = max > 0 ? (double) min / max : 0;
            widthListener = (obs, oldW, newW) -> minParticipantLine.setTranslateX(newW.doubleValue() * ratio);
            // initial positioning
            participantBar.applyCss(); participantBar.layout();
            minParticipantLine.setTranslateX(participantBar.getWidth() * ratio);
            participantBar.widthProperty().addListener(widthListener);

            // 3) Poster yükle
            String posterUrl = (String) data.get("posterUrl");
            if (posterUrl != null && !posterUrl.isEmpty()) {
                try {
                    eventImage.setImage(new Image(posterUrl, true));
                } catch (Exception e) {
                    System.err.println("⚠️ Poster yüklenemedi: " + posterUrl);
                }
            }

            // 4) Kulüp kartını ekle
            clubId = (String) data.get("clubId");
            if (clubId != null && !clubId.isEmpty()) {
                DocumentSnapshot clubDoc = FirestoreClient.getFirestore()
                    .collection("clubs")
                    .document(clubId)
                    .get()
                    .get();
                if (clubDoc.exists()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/club_card.fxml"));
                    Parent clubCard = loader.load();
                    ClubCardController ctl = loader.getController();
                    ctl.setData(clubDoc.getId(), clubDoc.getData());
                    clubCardPlaceholder.getChildren().add(clubCard);
                } else {
                    showClubError("Club not found: " + clubId);
                }
            } else {
                showClubError("No clubId provided.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showClubError("Error loading event detail.");
        }
    }

    private void showClubError(String msg) {
        Label lbl = new Label(msg);
        lbl.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        clubCardPlaceholder.getChildren().setAll(lbl);
    }

    private String getString(Map<String,Object> data, String key, String fallback) {
        Object o = data.get(key);
        return o instanceof String ? (String)o : fallback;
    }

    private int getNumber(Map<String,Object> data, String key, int fallback) {
        Object o = data.get(key);
        return (o instanceof Number) ? ((Number)o).intValue() : fallback;
    }

    /**
     * Back tuşuna basılınca ana sayfaya döner.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        // Eğer öğrenci ise student dashboard, yönetici ise admin dashboard vb. seçebilirsiniz:
        SceneChanger.switchScene(event, "main_dashboard.fxml");
    }

  
    // ... mevcut kodunuz

    /** Join’a tıklandığında şimdilik konsola yazı basar */
    @FXML
    private void onJoinClicked(ActionEvent event) {
        System.out.println("▶️ Join button clicked for event: " + eventNameLabel.getText());
        // TODO: buraya gerçek katılım işlevini ekle
    }

}