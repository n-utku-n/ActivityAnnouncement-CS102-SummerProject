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

/**
 * Controller class for the admin dashboard view.
 * This class handles UI interactions for admin-related tasks such as
 * viewing users, events, and club information from Firebase.
 * @author Utku
 */
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

    /**
     * Initializes the admin dashboard UI. Called automatically after FXML is loaded.
     * Sets up combo box filter options. 
     * @author Utku
     */
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

         /**
     * Loads and displays all clubs from the Firestore database.
     * Populates the clubsListBox with club cards containing name and description.
     */
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

     /**
     * Loads and filters user data from Firestore, then displays it.
     *
     * @param keyword     Keyword to search for in user names or emails.
     * @param roleFilter  Selected role to filter users by (All, student, club_manager).
     */
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

     /**
     * Loads and displays all events from Firestore.
     * Populates eventListBox with event cards.
     */
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

     /**
     * Updates a user's role in Firestore and refreshes the view.
     *
     * @param uid      User document ID
     * @param newRole  The new role to assign (student or club_manager)
     */
    private void updateRole(String uid, String newRole) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("users").document(uid).update("role", newRole);
        refreshButton.fire();
    }

     /**
     * Deletes a user from Firestore and refreshes the dashboard.
     *
     * @param uid User document ID to delete
     */
    private void deleteUser(String uid) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("users").document(uid).delete();
        refreshButton.fire();
    }

    /**
     * Deletes an event from Firestore and refreshes the dashboard.
     *
     * @param eventId Event document ID to delete
     */
    private void deleteEvent(String eventId) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("events").document(eventId).delete();
        refreshButton.fire();
    }
}
