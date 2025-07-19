package com.project1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import com.project1.SceneChanger;
import com.project1.EventDetailController;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CommentsController {

    @FXML private Button backButton;
    @FXML private Label eventNameLabel;
    @FXML private VBox commentsContainer;
    @FXML private Button addCommentButton;
    @FXML private HBox averageStarsBox;

    private String eventId;
    private String eventName;
    private String currentUserDisplayName = System.getProperty("user.name", "Anon");
    private String currentUserId;
    private UserModel currentUser;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "comments-firestore-io");
        t.setDaemon(true);
        return t;
    });

    @FXML
    private void initialize() {
        // CommentsController kendisini sahnenin köküne bağlar
        try {
            backButton.getScene().getRoot().setUserData(this);
        } catch (Exception ignored) { /* Sahne henüz yoksa hata verme */ }
    }

    /** Event bilgilerini ayarlar ve yorumları yükler */
    public void setEventContext(String eventId, String eventName) {
        this.eventId = eventId;
        this.eventName = eventName;
        eventNameLabel.setText(eventName != null ? eventName : "Event");
        loadCommentsAsync();
    }

    /** Login aşamasında mutlaka çağırılmalı */
    public void setCurrentUser(UserModel user) {
        System.out.println("CommentsController setCurrentUser: " + user);
        this.currentUser = user;
        if (user == null) {
            System.err.println("[CommentsController] setCurrentUser called with null user, ignoring.");
            return;
        }
        
        System.out.println("AddCommentController setCurrentUser: " + user);
        String displayName = user.getName()
                             + (user.getSurname().isEmpty() ? "" : " " + user.getSurname());
        this.currentUserDisplayName = displayName;
        this.currentUserId = user.getStudentId();
    }

   @FXML
    private void handleBack(ActionEvent e) {
        if (currentUser == null) {
            System.err.println("⚠️ Yorum ekleyemezsin, kullanıcı null!");
            return;
        }
        FXMLLoader loader = SceneChanger.switchScene(e, "event_detail.fxml");
        EventDetailController edc = loader.getController();
        edc.setLoggedInUser(currentUser);
        edc.setEventId(eventId);
    }

    @FXML
    private void handleAddComment(ActionEvent e) {
        if (eventId == null || eventId.isBlank()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/addCommentPage.fxml"));
            Parent root = loader.load();
            AddCommentController acc = loader.getController();

            // Her zaman güncel user model aktar
            UserModel userModel = (currentUser != null) ? currentUser :
                new UserModel(currentUserDisplayName, "", 
                    (currentUserId != null && !currentUserId.isBlank() ? currentUserId : ""),
                    "", "");

            acc.setCurrentUser(userModel);
            acc.setEventContext(eventId, eventName);

            Stage stage = (Stage) addCommentButton.getScene().getWindow();
            root.setUserData(this); // AddCommentPage köküne CommentsController referansını ekle
            stage.getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadCommentsAsync() {
        commentsContainer.getChildren().clear();
        if (eventId == null || eventId.isBlank()) return;
        CompletableFuture
            .supplyAsync(() -> loadCommentsBlocking(eventId), ioExecutor)
            .thenAccept(list -> Platform.runLater(() -> renderComments(list)))
            .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private List<CommentRecord> loadCommentsBlocking(String eventId) {
        List<CommentRecord> records = new ArrayList<>();
        try {
            var db  = FirestoreClient.getFirestore();
            var col = db.collection("events")
                        .document(eventId)
                        .collection("comments");
            ApiFuture<QuerySnapshot> fut = col.get();
            QuerySnapshot snap = fut.get();

            for (QueryDocumentSnapshot doc : snap.getDocuments()) {
                String first = doc.getString("firstName");
                String last  = doc.getString("lastName");
                String user  = (first != null && last != null)
                    ? first + " " + last
                    : (doc.getString("userName") != null ? doc.getString("userName") : "Anon");

                records.add(new CommentRecord(
                    doc.getId(),
                    eventId,
                    user,
                    doc.getString("text"),
                    doc.getTimestamp("timestamp"),
                    doc.contains("rating") ? doc.getDouble("rating") : 0.0,
                    doc.getString("userPhotoUrl"),
                    doc.getString("studentNo"),
                    doc.getString("role")
                ));
            }

            records.sort(Comparator
                .comparingDouble((CommentRecord cr) -> cr.rating).reversed()
                .thenComparing(cr -> cr.timestamp == null ? 0L : cr.timestamp.getSeconds(),
                               Comparator.reverseOrder())
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return records;
    }

    private void renderComments(List<CommentRecord> comments) {
        averageStarsBox.getChildren().clear();
        double avg = comments.stream().mapToDouble(cr -> cr.rating).average().orElse(0.0);
        int full = (int)Math.round(avg);
        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < full ? "★" : "☆");
            star.getStyleClass().add("comments-average-star");
            averageStarsBox.getChildren().add(star);
        }
        commentsContainer.getChildren().clear();
        if (comments.isEmpty()) {
            commentsContainer.getChildren().add(new Label("No comments yet."));
        } else {
            comments.forEach(cr ->
                commentsContainer.getChildren().add(buildCommentCard(cr))
            );
        }
        // YORUM ENGELİNİ studentNo'ya göre yap!
        boolean has = comments.stream().anyMatch(c ->
            c.studentNo != null &&
            currentUserId != null &&
            c.studentNo.equals(currentUserId)
        );
        addCommentButton.setManaged(!has);
        addCommentButton.setVisible(!has);
    }

    private javafx.scene.Node buildCommentCard(CommentRecord cr) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/comment_card.fxml"));
            Parent card = loader.load();
            CommentCardController ctrl = loader.getController();
            ctrl.setData(
                cr.id,
                cr.eventId,
                cr.userName,
                cr.studentNo,
                cr.role,
                cr.text,
                (int)cr.rating,
                cr.timestamp,
                currentUserDisplayName
            );
            ctrl.setCurrentUserId(currentUserId);
            ctrl.setRefreshCommentsCallback(this::loadCommentsAsync);
            // Eğer CommentCardController içinde UserModel'e ihtiyaç varsa burada iletebilirsin!
            // ctrl.setCurrentUser(currentUser);
            return new VBox(card);
        } catch (IOException e) {
            e.printStackTrace();
            return new Label("Error loading comment");
        }
    }

    private static final class CommentRecord {
        final String id, eventId, userName, text, userPhotoUrl, studentNo, role;
        final Timestamp timestamp;
        final double rating;
        CommentRecord(String id, String ev, String u, String t, Timestamp ts,
                      double r, String photo, String stu, String role) {
            this.id = id; this.eventId = ev; this.userName = u; this.text = t;
            this.timestamp = ts; this.rating = r;
            this.userPhotoUrl = photo; this.studentNo = stu; this.role = role;
        }
    }
}