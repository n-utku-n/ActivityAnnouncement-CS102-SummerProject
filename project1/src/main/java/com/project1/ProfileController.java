package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.Timestamp;
import com.google.firebase.cloud.FirestoreClient;

public class ProfileController {

    @FXML
    private Button backButton;
    @FXML
    private Label nameLabel;
    @FXML
    private Label surnameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label roleLabel;

    @FXML
    private ScrollPane joinedEventsScrollPane;
    @FXML
    private GridPane joinedEventsContainer;

    private UserModel loggedInUser;

    @FXML
    private void initialize() {
        // Back button click handler (SceneChanger ile)
        backButton.setOnAction(this::handleBackButton);
    }

    private void handleBackButton(ActionEvent event) {
        // SceneChanger ile ana ekrana (main_dashboard.fxml) geçiş
        SceneChanger.switchScene(event, "main_dashboard.fxml", controller -> {
            if (controller instanceof MainDashboardController mainDash) {
                mainDash.setUser(loggedInUser); // Veya setLoggedInUser(loggedInUser)
            }
        });
    }

    public void setUser(UserModel loggedInUser) {
        if (loggedInUser != null) {
            this.loggedInUser = loggedInUser;
            nameLabel.setText(loggedInUser.getName() != null ? loggedInUser.getName() : "N/A");
            surnameLabel.setText(loggedInUser.getSurname() != null ? loggedInUser.getSurname() : "N/A");
            emailLabel.setText(loggedInUser.getEmail() != null ? loggedInUser.getEmail() : "N/A");
            roleLabel.setText(loggedInUser.getRole() != null ? loggedInUser.getRole() : "N/A");

            loadJoinedEvents(loggedInUser.getStudentId());
        }
    }

    private void loadJoinedEvents(String studentId) {
        if (studentId == null || studentId.isEmpty()) {
            System.out.println("Student ID yok, katıldığı etkinlikler yüklenemedi.");
            return;
        }

        Firestore db = FirestoreClient.getFirestore();
        joinedEventsContainer.getChildren().clear();

        CompletableFuture.runAsync(() -> {
            try {
                QuerySnapshot eventsSnap = db.collection("events").get().get();
                List<QueryDocumentSnapshot> docs = eventsSnap.getDocuments();
                long currentTimestamp = System.currentTimeMillis();

                int col = 0;
                int row = 0;
                for (QueryDocumentSnapshot doc : docs) {
                    List<String> participants = (List<String>) doc.get("participants");
                    if (participants != null && participants.contains(studentId)) {
                        Timestamp eventDate = doc.getTimestamp("eventDate");
                        if (eventDate == null || eventDate.toDate().getTime() < currentTimestamp) {
                            continue; // Geçmiş etkinlikleri atla
                        }
                        Map<String, Object> data = doc.getData();
                        final int currentCol = col;
                        final int currentRow = row;
                        javafx.application.Platform.runLater(() -> {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
                                Parent eventCard = loader.load();
                                EventCardController controller = loader.getController();
                                controller.setCurrentUser(loggedInUser);
                                controller.setData(doc.getId(), data);

                                joinedEventsContainer.add(eventCard, currentCol, currentRow);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        col++;
                        if (col >= 2) {
                            col = 0;
                            row++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}