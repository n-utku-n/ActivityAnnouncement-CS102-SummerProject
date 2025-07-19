package com.project1;

import com.project1.SceneChanger;
import com.project1.UserModel;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import javafx.scene.Scene;
import javafx.scene.Node;
import com.project1.MainDashboardController;

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

    @FXML private Button joinButton;
    @FXML private Button commentsButton;
    @FXML
    private Button quitButton;
    private String eventId;
    private String clubId;

    // Kullanıcı bilgileri
    private String currentUserName = System.getProperty("user.name", "Anon");
    private String currentUserId;
    private UserModel loggedInUser;

  @FXML
private void onQuitClicked(ActionEvent event) {
    if (eventId == null || eventId.trim().isEmpty() || currentUserId == null || currentUserId.trim().isEmpty()) {
        System.err.println("Quit işlemi için eventId veya userId eksik.");
        return;
    }

    Firestore db = FirestoreClient.getFirestore();
    DocumentReference eventRef = db.collection("events").document(eventId);

    CompletableFuture.runAsync(() -> {
        try {
            db.runTransaction(transaction -> {
                DocumentSnapshot snap = transaction.get(eventRef).get();
                if (!snap.exists()) {
                    throw new RuntimeException("Event bulunamadı: " + eventId);
                }

                List<String> participants = (List<String>) snap.get("participants");
                Long currentCount = snap.contains("currentParticipants") ? snap.getLong("currentParticipants") : 0L;

                if (participants == null || !participants.contains(currentUserId)) {
                    System.out.println("Kullanıcı etkinlikte değil, çıkış işlemi atlandı.");
                    return null;
                }

                // Find comments by this user
                var commentsCol = eventRef.collection("comments");
                var commentsQuery = commentsCol.whereEqualTo("studentNo", currentUserId).get().get();

                // Initialize rating sums
                double ratingSum = snap.contains("ratingSum") ? snap.getDouble("ratingSum") : 0.0;
                long ratingCount = snap.contains("ratingCount") ? snap.getLong("ratingCount") : 0;

                // Remove user's comments and update rating sums
                for (var doc : commentsQuery.getDocuments()) {
                    double rating = doc.contains("rating") ? doc.getDouble("rating") : 0.0;
                    ratingSum -= rating;
                    ratingCount = Math.max(0, ratingCount - 1);
                    transaction.delete(doc.getReference());
                }

                participants.remove(currentUserId);

                double newAvg = ratingCount > 0 ? ratingSum / ratingCount : 0.0;

                transaction.update(eventRef,
                    "participants", participants,
                    "currentParticipants", Math.max(0, currentCount - 1),
                    "ratingSum", ratingSum,
                    "ratingCount", ratingCount,
                    "averageRating", newAvg
                );

                return null;
            }).get();

            Platform.runLater(() -> {
                quitButton.setVisible(false);  // Quit butonunu gizle
                setEventId(eventId);            // Event detayları yeniden yüklensin
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.runLater(() -> {
                System.err.println("Etkinlikten ayrılırken hata: " + ex.getMessage());
            });
        }
    });
}
    /**
     * Login sonrası mutlaka çağırılmalı:
     * oturum açmış UserModel nesnesini saklar.
     */
    public void setLoggedInUser(UserModel user) {
        this.loggedInUser = user;
        this.currentUserName = user.getName()
            + (user.getSurname().isEmpty() ? "" : " " + user.getSurname());
        this.currentUserId = user.getStudentId();
    }

    /**
     * Etkinlik verilerini UI'a yansıtır.
     */
   public void setEventData(String eventId, Map<String, Object> data) {
    this.eventId = eventId;

    // Başlık ve metinler
    eventNameLabel.setText(getString(data, "name", "Unnamed Event"));
    descriptionLabel.setText(getString(data, "description", ""));
    rulesLabel.setText(getString(data, "rules", ""));

    clubCardPlaceholder.getChildren().clear();

    // Kulüp kartı (sadece bir kez yüklenecek)
    String clubId = getString(data, "clubId", null);
    if (clubId != null && !clubId.isEmpty()) {
        try {
            DocumentSnapshot clubDoc = FirestoreClient
                .getFirestore()
                .collection("clubs")
                .document(clubId)
                .get()
                .get();
            if (clubDoc.exists()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/club_card.fxml"));
                Parent clubCard = loader.load();
                ClubCardController ctl = loader.getController();
                ctl.setData(clubDoc.getId(), clubDoc.getData());
                ctl.setPreviousEventId(this.eventId);
                clubCardPlaceholder.getChildren().add(clubCard);
            } else {
                showClubError("Club not found: " + clubId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showClubError("Error loading club.");
        }
    } else {
        showClubError("No clubId provided.");
    }

    // Katılımcı bilgisi
    List<String> participantsList = (List<String>) data.get("participants");
    int current = (participantsList != null ? participantsList.size() : 0);
    int min = getNumber(data, "minParticipants", 0);
    int max = getNumber(data, "maxParticipants", 0);
    participantInfoLabel.setText(
        String.format("👥 Participants: %d (Min: %d, Max: %d)", current, min, max)
    );
    double progress = max > 0 ? (double) current / max : 0;
    participantBar.setProgress(progress);

    // Min çizgisi pozisyonu
    if (widthListener != null) {
        participantBar.widthProperty().removeListener(widthListener);
    }
    final double ratio = max > 0 ? (double) min / max : 0;
    widthListener = (obs, o, n) -> minParticipantLine.setTranslateX(n.doubleValue() * ratio);
    participantBar.applyCss();
    participantBar.layout();
    minParticipantLine.setTranslateX(participantBar.getWidth() * ratio);
    participantBar.widthProperty().addListener(widthListener);

    // Quit butonunun görünürlüğünü katılım durumuna göre ayarla
    boolean hasJoined = participantsList != null && participantsList.contains(currentUserId);
    quitButton.setVisible(hasJoined);
    quitButton.setManaged(hasJoined);

    // Join / Comments butonlarının durumu
    if (current >= max) {
        if (hasJoined) {
            joinButton.setText("Show Comments");
            joinButton.setDisable(false);
            joinButton.setOnAction(this::onCommentsClicked);
        } else {
            joinButton.setText("Event Full");
            joinButton.setDisable(true);
            joinButton.setOnAction(null);
        }
        commentsButton.setVisible(false);
        commentsButton.setManaged(false);
    } else {
        if (hasJoined) {
            joinButton.setText("Show Comments");
            joinButton.setOnAction(this::onCommentsClicked);
        } else {
            joinButton.setText("Join");
            joinButton.setOnAction(this::onJoinClicked);
        }
        joinButton.setDisable(false);
        commentsButton.setVisible(false);
        commentsButton.setManaged(false);
    }

    // Poster
    String posterUrl = getString(data, "posterUrl", null);
    if (posterUrl != null && !posterUrl.isEmpty()) {
        try {
            eventImage.setImage(new Image(posterUrl, true));
        } catch (Exception e) {
            System.err.println("⚠️ Poster yüklenemedi: " + posterUrl);
        }
    }
}

    private void showClubError(String msg) {
        Label lbl = new Label(msg);
        lbl.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        clubCardPlaceholder.getChildren().setAll(lbl);
    }

    private String getString(Map<String,Object> data, String key, String fallback) {
        Object o = data.get(key);
        return (o instanceof String) ? (String)o : fallback;
    }

    private int getNumber(Map<String,Object> data, String key, int fallback) {
        Object o = data.get(key);
        return (o instanceof Number) ? ((Number)o).intValue() : fallback;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        // Smoothly swap back to main dashboard without resizing the window
        FXMLLoader loader = SceneChanger.switchScene(event, "main_dashboard.fxml");
        if (loader != null) {
            MainDashboardController controller = loader.getController();
            controller.setLoggedInUser(loggedInUser);
        }
    }

    /** Katıl butonuna tıklanınca */
    @FXML
    private void onJoinClicked(ActionEvent event) {
        if (eventId == null || eventId.trim().isEmpty()) return;
        // Prevent multiple joins
        try {
            DocumentSnapshot snap = FirestoreClient.getFirestore()
                .collection("events").document(eventId).get().get();
            List<String> participantsList = (List<String>) snap.get("participants");
            if (participantsList != null && participantsList.contains(currentUserId)) {
                // Already joined, just show comments
                onCommentsClicked(event);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Firestore'da katılımcıyı +1 artır ve kullanıcıyı listeye ekle
        try {
            Firestore db = FirestoreClient.getFirestore();
            db.collection("events")
              .document(eventId)
              .update(
                  "participants", FieldValue.arrayUnion(currentUserId),
                  "currentParticipants", FieldValue.increment(1)
              )
              .get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Comments ekranına geç
        FXMLLoader loader = SceneChanger.switchScene(event, "comments.fxml");
        if (loader != null) {
            CommentsController cc = loader.getController();
            cc.setCurrentUser(loggedInUser);
            cc.setEventContext(eventId, eventNameLabel.getText());
        }
    }

    /** Comments butonuna tıklanınca */
   @FXML
    private void onCommentsClicked(ActionEvent event) {
        FXMLLoader loader = SceneChanger.switchScene(event, "comments.fxml");
        if (loader != null) {
            CommentsController cc = loader.getController();
            cc.setCurrentUser(loggedInUser);
            cc.setEventContext(eventId, eventNameLabel.getText());
        }
    }

    /**
     * Called when navigating back from ClubProfile to reload this event.
     */
    public void setEventId(String eventId) {
        // Guard against empty or null IDs
        if (eventId == null || eventId.trim().isEmpty()) {
            System.err.println("Warning: setEventId received empty eventId, skipping reload.");
            return;
        }
        this.eventId = eventId;
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentSnapshot snap = db.collection("events").document(eventId).get().get();
            if (snap.exists()) {
                setEventData(eventId, snap.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}