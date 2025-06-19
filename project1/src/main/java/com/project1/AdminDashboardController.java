package com.project1;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class AdminDashboardController {

    @FXML
    private Button profileButton;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private Button refreshButton;

    @FXML
    private VBox userListBox;

    @FXML
    private VBox eventListBox;

    @FXML
    private VBox clubsListBox;

    @FXML
    private void initialize() {
        System.out.println("📋 Admin dashboard initialized");

        filterComboBox.getItems().addAll("All", "student", "club_manager", "pending");
        filterComboBox.setValue("All");

        loadClubs();
        loadUsers("", "All");
        loadEvents();

        refreshButton.setOnAction(e -> {
            loadClubs();
            loadUsers(searchField.getText().trim(), filterComboBox.getValue());
            loadEvents();
        });

        searchField.textProperty().addListener((observable, oldVal, newVal) -> {
            loadUsers(newVal.trim(), filterComboBox.getValue());
        });

        filterComboBox.valueProperty().addListener((observable, oldVal, newVal) -> {
            loadUsers(searchField.getText().trim(), newVal);
        });
    }

    private void loadClubs() {
        clubsListBox.getChildren().clear();
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection("clubs").get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                VBox card = new VBox(5);
                card.setStyle("-fx-border-color: gray; -fx-padding: 10;");
                Label name = new Label("Club: " + doc.getString("name"));
                Label description = new Label("About: " + doc.getString("description"));
                card.getChildren().addAll(name, description);
                clubsListBox.getChildren().add(card);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers(String keyword, String roleFilter) {
        userListBox.getChildren().clear();
        Firestore db = FirestoreClient.getFirestore();

        CollectionReference usersRef = db.collection("users");
        ApiFuture<QuerySnapshot> future = usersRef.get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                String name = doc.getString("name");
                String email = doc.getString("email");
                String role = doc.getString("role");

                if (!keyword.isEmpty() && !(name.toLowerCase().contains(keyword.toLowerCase()) || email.toLowerCase().contains(keyword.toLowerCase()))) {
                    continue;
                }

                if (!roleFilter.equals("All") && !role.equals(roleFilter)) {
                    continue;
                }

                HBox row = new HBox(10);
                Label info = new Label(name + " - " + role);

                Button details = new Button("Details");

                Button toggleRole = new Button("Change Role");
                toggleRole.setOnAction(e -> {
                    String newRole = role.equals("student") ? "club_manager" : "student";
                    updateRole(doc.getId(), newRole);
                });

                Button delete = new Button("Delete");
                delete.setOnAction(e -> deleteUser(doc.getId()));

                row.getChildren().addAll(info, details, toggleRole, delete);
                userListBox.getChildren().add(row);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void loadEvents() {
        eventListBox.getChildren().clear();
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection("events").get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                String title = doc.getString("title");
                String date = doc.getString("date");

                HBox row = new HBox(10);
                Label info = new Label(title + " - " + date);
                Button edit = new Button("Edit");
                Button delete = new Button("Delete");

                delete.setOnAction(e -> deleteEvent(doc.getId()));

                row.getChildren().addAll(info, edit, delete);
                eventListBox.getChildren().add(row);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void updateRole(String uid, String newRole) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("users").document(uid).update("role", newRole);
        refreshButton.fire();
    }

    private void deleteUser(String uid) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("users").document(uid).delete();
        refreshButton.fire();
    }

    private void deleteEvent(String eventId) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("events").document(eventId).delete();
        refreshButton.fire();
    }
}
