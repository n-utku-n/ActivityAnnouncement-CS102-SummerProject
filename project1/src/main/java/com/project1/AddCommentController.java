package com.project1;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

/**
 * Initialize context by fetching event name from Firestore.
 */
public class AddCommentController {
    @FXML private Button backButton;
    @FXML private Label eventNameLabel;
    @FXML private Label userInfoLabel;
    @FXML private HBox starsBox;
    @FXML private TextArea commentTextArea;
    @FXML private Button submitButton;

    private String eventId;
    private String eventName;
    private UserModel currentUser;
    private int selectedRating = 0;
    private String editingCommentId = null;
    private String originalText = null;
    private int originalRating = 0;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "add-comment-io");
        t.setDaemon(true);
        return t;
    });

    public void setEventContext(String eventId) {
        this.eventId = eventId;
        CompletableFuture.runAsync(() -> {
            try {
                Firestore db = FirestoreClient.getFirestore();
                DocumentSnapshot snap = db.collection("events").document(eventId).get().get();
                String name = snap.exists() ? snap.getString("name") : "";
                Platform.runLater(() -> {
                    this.eventName = name;
                    eventNameLabel.setText(name);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /** Etkinlik bilgisini alır ve başlık etiketini günceller. */
    public void setEventContext(String eventId, String eventName) {
        this.eventId = eventId;
        this.eventName = eventName;
        eventNameLabel.setText(eventName != null ? eventName : "");
    }

    /**
     * Oturum açmış kullanıcı modelini ayarlar ve başlıkta gösterir.
     */
    public void setCurrentUser(UserModel user) {
        System.out.println("AddCommentController setCurrentUser: " + user);
        this.currentUser = user;

        // Ad Soyad – Öğrenci No – Rol şeklinde label
        String namePart = user.getName() != null ? user.getName() : "N/A";
        String surnamePart = user.getSurname() != null && !user.getSurname().isEmpty()
                             ? user.getSurname() : "N/A";
        String studentIdPart = user.getStudentId() != null && !user.getStudentId().isEmpty()
                                ? user.getStudentId() : "N/A";
        String rolePart = user.getRole() != null && !user.getRole().isEmpty()
                            ? user.getRole() : "N/A";

        String display = String.format(
            "%s %s – ID: %s – Role: %s",
            namePart, surnamePart, studentIdPart, rolePart
        );
        userInfoLabel.setText(display);
    }

    @FXML
    private void initialize() {
        // 5 tıklanabilir yıldız oluştur
       
        IntStream.rangeClosed(1, 5)
                 .forEach(i -> {
                     Label star = new Label("☆");
                     star.setOnMouseClicked(e -> setRating(i));
                     starsBox.getChildren().add(star);
                 });
    }

    /**
     * Switch to edit mode with existing comment data.
     */
    public void setEditMode(String commentId, String existingText, int existingRating) {
        this.editingCommentId = commentId;
        // Orijinal verileri kaydet
        this.originalText = existingText;
        this.originalRating = existingRating;
        commentTextArea.setText(existingText);
        setRating(existingRating);
        submitButton.setText("Edit");
    }

    private void setRating(int rating) {
        this.selectedRating = rating;
        for (int i = 0; i < starsBox.getChildren().size(); i++) {
            Label star = (Label) starsBox.getChildren().get(i);
            star.setText(i < rating ? "★" : "☆");
        }
    }

@FXML
private void handleBack() {
    System.out.println("handleBack: currentUser=" + currentUser);
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/comments.fxml"));
        Parent root = loader.load();
        CommentsController cc = loader.getController();
        cc.setCurrentUser(currentUser);
        cc.setEventContext(eventId, eventName);
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.getScene().setRoot(root);
    } catch (IOException ex) {
        ex.printStackTrace();
    }
}

    @FXML
    private void handleSubmit() {
           // Eğer edit modundaysak, ama metin ve puan orijinalle aynıysa hiçbir şey yapma, sadece geri dön
        String newText = commentTextArea.getText().trim();
        if (editingCommentId != null
         && newText.equals(originalText)
         && selectedRating == originalRating) {
            closeWithSuccess();
            return;
        }
        if (eventId == null || eventId.isBlank()
         || currentUser == null
         || currentUser.getStudentId() == null
         || currentUser.getStudentId().isBlank()) {
            new Alert(Alert.AlertType.ERROR,
                "Cannot add comment: missing event or user info.",
                ButtonType.OK).show();
            return;
        }
        String text = commentTextArea.getText().trim();
        if (text.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                "Comment cannot be empty.", ButtonType.OK).show();
            return;
        }
        if (editingCommentId != null) {
            updateComment(text, selectedRating);
        } else {
            saveNewComment(text, selectedRating);
        }
    }

    private void saveNewComment(String text, int rating) {
    String evId = eventId;
    Timestamp now = Timestamp.now();

    CompletableFuture.runAsync(() -> {
        try {
            var db = FirestoreClient.getFirestore();
            DocumentReference eventRef = db.collection("events").document(evId);
            CollectionReference commentsCol = eventRef.collection("comments");

            // 1) Yeni yorumu ekle
            Map<String,Object> data = new HashMap<>();
            data.put("eventId",   evId);
            data.put("userName",  currentUser.getName() 
                                + (currentUser.getSurname().isEmpty() ? "" : " " + currentUser.getSurname()));
            data.put("firstName", currentUser.getName());
            data.put("lastName",  currentUser.getSurname());
            data.put("studentNo", currentUser.getStudentId());
            data.put("role",      currentUser.getRole());
            data.put("text",      text);
            data.put("rating",    (double) rating);
            data.put("timestamp", now);

            commentsCol.add(data).get();

            // 2) Event belgesinde transaction ile rating ve participant sayısını güncelle
            db.runTransaction(transaction -> {
                DocumentSnapshot eventSnap = transaction.get(eventRef).get();

                long ratingCount = eventSnap.contains("ratingCount") ? eventSnap.getLong("ratingCount") : 0;
                double ratingSum = eventSnap.contains("ratingSum") ? eventSnap.getDouble("ratingSum") : 0.0;
                long currentParticipants = eventSnap.contains("currentParticipants") ? eventSnap.getLong("currentParticipants") : 0;

                long newRatingCount = ratingCount + 1;
                double newRatingSum = ratingSum + rating;
                long newCurrentParticipants = currentParticipants + 1;

                double newAverage = newRatingCount > 0 ? newRatingSum / newRatingCount : 0.0;

                transaction.update(eventRef, 
                    "ratingCount", newRatingCount,
                    "ratingSum", newRatingSum,
                    "averageRating", newAverage,
                    "currentParticipants", newCurrentParticipants);

                return null;
            }).get();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }, ioExecutor)
    .thenRun(() -> Platform.runLater(this::closeWithSuccess))
    .exceptionally(ex -> {
        Platform.runLater(() -> new Alert(
            Alert.AlertType.ERROR,
            "Yorum ekleme başarısız: " + ex.getMessage(),
            ButtonType.OK
        ).show());
        return null;
    });
}

private void updateComment(String text, int rating) {
    String evId = eventId;
    String commentId = editingCommentId;
    Timestamp now = Timestamp.now();
    CompletableFuture.runAsync(() -> {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference eventRef = db.collection("events").document(evId);
            DocumentReference commentRef = eventRef.collection("comments").document(commentId);

            db.runTransaction(transaction -> {
                // 1. Eski rating'i bul
                DocumentSnapshot commentSnap = transaction.get(commentRef).get();
                double oldRating = commentSnap.contains("rating") ? commentSnap.getDouble("rating") : 0.0;

                // 2. Event'in mevcut ratingSum ve ratingCount'unu bul
                DocumentSnapshot eventSnap = transaction.get(eventRef).get();
                double ratingSum = eventSnap.contains("ratingSum") ? eventSnap.getDouble("ratingSum") : 0.0;
                long ratingCount = eventSnap.contains("ratingCount") ? eventSnap.getLong("ratingCount") : 0;

                // 3. Yeni toplam ve ortalamayı hesapla
                double newSum = ratingSum - oldRating + rating;
                double newAvg = ratingCount > 0 ? newSum / ratingCount : 0.0;

                // 4. Event'i güncelle
                transaction.update(eventRef, "ratingSum", newSum, "averageRating", newAvg);

                // 5. Yorumu güncelle
                Map<String, Object> updates = new HashMap<>();
                updates.put("text", text);
                updates.put("rating", (double) rating);
                updates.put("timestamp", now);
                transaction.update(commentRef, updates);

                return null;
            }).get();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }, ioExecutor)
    .thenRun(() -> Platform.runLater(this::closeWithSuccess))
    .exceptionally(ex -> {
        Platform.runLater(() -> new Alert(
            Alert.AlertType.ERROR,
            "Yorum güncelleme başarısız: " + ex.getMessage(),
            ButtonType.OK
        ).show());
        return null;
    });
}

    private void closeWithSuccess() {
        handleBack();
    }
}