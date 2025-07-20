package com.project1;

import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
// width listener'ı tutmak için
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import javafx.application.Platform;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import java.util.concurrent.CompletableFuture;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.project1.UserModel;
import com.project1.EventDetailController;

/**
 * Controller for the event card UI component.
 * Displays brief information about an event and allows users
 * to navigate to its detail page.
 *
 * @author Utku, Serra
 */
public class EventCardController {
private javafx.beans.value.ChangeListener<Number> widthListener;

    @FXML
    private StackPane photoWrapper;

    @FXML
    private Rectangle photoClip;

    @FXML 
    private StackPane progressContainer;

    @FXML
    private VBox eventCardRoot;

    @FXML 
    private ProgressBar participantBar;

    @FXML 
    private Rectangle minParticipantLine;
    
    @FXML
    private StackPane participantContainer; // CHANGED: Updated to match new FXML

    @FXML 
    private Label currentParticipantsText;

    @FXML
    private Label maxParticipantsText;

    @FXML
    private Label eventFullLabel;

    @FXML 
    private Label eventName;

    @FXML 
    private Label eventDateText;
    @FXML
    private HBox ratingBox;
    @FXML
    private Label ratingStarsLabel;
    @FXML
    private Label ratingValueLabel;


    // REMOVED: eventDateText is no longer in the design
    // @FXML
    // private Label eventDateText;

    @FXML 
    private Label clubName;

    @FXML 
    private ImageView eventImage;

    @FXML 
    private ImageView clubLogo;

    private boolean isEditMode = false;
    private UserModel currentUser;
    public void setCurrentUser(UserModel user) { this.currentUser = user; }




    /**
     * Event card loading whether based on clicked or not
     * @author Serra
     */
   @FXML
private void onCardClicked(MouseEvent event) {
   
    System.out.println("📦 Event card clicked!");
    
    if (eventId != null && eventData != null && currentUser != null) {
        navigateToEventDetail();
    } else {
        System.err.println("❌ EventCardController: Missing data! eventId, eventData or currentUser is null.");
    }
}

    /** Firestore event document ID */
    private String eventId;

    /** Firestore club document ID */
    private String clubId;

    /** Cached event data for later use */
    private Map<String, Object> eventData;
    

    /**
     * Populates the event card UI with provided event data.
     * Sets all relevant text fields, progress bar, and loads images.
     *
     * @param eventId Firestore document ID of the event
     * @param data    Map containing event fields like name, participants, image URLs, etc.
     */
    public void setData(String eventId, Map<String, Object> data) {
        System.out.println("👉 Setting data for event card");
        System.out.println("📌 participantContainer null check: " + (participantContainer == null));
    
        // Initialize components if they're null (fallback lookup)
        initializeComponents();
        
        // Validate required components
        if (!validateComponents()) {
            System.out.println("❌ Required components are missing, cannot populate card");
            return;
        }

        this.eventId = eventId;
        this.eventData = data;
        // Ensure eventFullLabel is hidden by default before populating
        if (eventFullLabel != null) {
            eventFullLabel.setVisible(false);
            eventFullLabel.setManaged(false);
        }

        // Set event title and organizing club name
        populateEventDetails(data);
        
        // Set participant information and progress bar
        populateParticipantInfo(data);

        // Load images
        loadImages(data);
        showAverageRating(data);
        // Show "Event Full" if participants reached max (done inside populateParticipantInfo)
    }

    private void showAverageRating(Map<String, Object> data) {
    double avg = 0.0;
    if (data.get("averageRating") instanceof Number) {
        avg = ((Number) data.get("averageRating")).doubleValue();
    }
    // Yıldızları oluştur
    StringBuilder stars = new StringBuilder();
    int full = (int) avg;
    for (int i = 0; i < full; i++) stars.append("★");
    if (avg - full >= 0.5) stars.append("☆"); // Yarım yıldız da olabilir
    while (stars.length() < 5) stars.append("☆");

    ratingStarsLabel.setText(stars.toString());
    ratingValueLabel.setText(String.format("%.1f", avg));
    
    // Görünürlüğü ayarla
    if (avg > 0) {
        ratingBox.setVisible(true);
    } else {
        ratingBox.setVisible(false);
    }
}
    /**
     * Initialize components using lookup if FXML injection failed
     */
    private void initializeComponents() {

        if (progressContainer == null) {
            progressContainer = (StackPane) eventCardRoot.lookup("#progressContainer");
            System.out.println("🔁 lookup progressContainer: " + (progressContainer != null));
        }
        if (eventCardRoot == null) {
            System.out.println("❌ eventCardRoot is null - FXML injection failed");
            return;
        }

        if (clubName == null) {
            clubName = (Label) eventCardRoot.lookup("#clubName");
            System.out.println("🔁 lookup clubName: " + (clubName != null));
        }

        if (eventName == null) {
            eventName = (Label) eventCardRoot.lookup("#eventName");
            System.out.println("🔁 lookup eventName: " + (eventName != null));
        }

        if (eventDateText == null) {
            eventDateText = (Label) eventCardRoot.lookup("#eventDateText");
            System.out.println("🔁 lookup eventDateText: " + (eventDateText != null));
        }

        if (participantBar == null) {
            participantBar = (ProgressBar) eventCardRoot.lookup("#participantBar");
            System.out.println("🔁 lookup participantBar: " + (participantBar != null));
        }

        if (minParticipantLine == null) {
            minParticipantLine = (Rectangle) eventCardRoot.lookup("#minParticipantLine");
            System.out.println("🔁 lookup minParticipantLine: " + (minParticipantLine != null));
        }

        if (participantContainer == null) {
            participantContainer = (StackPane) eventCardRoot.lookup("#participantContainer");
            System.out.println("🔁 lookup participantContainer: " + (participantContainer != null));
        }

        if (currentParticipantsText == null) {
            currentParticipantsText = (Label) eventCardRoot.lookup("#currentParticipantsText");
            System.out.println("🔁 lookup currentParticipantsText: " + (currentParticipantsText != null));
        }

        if (maxParticipantsText == null) {
            maxParticipantsText = (Label) eventCardRoot.lookup("#maxParticipantsText");
            System.out.println("🔁 lookup maxParticipantsText: " + (maxParticipantsText != null));
        }

        if (eventImage == null) {
            eventImage = (ImageView) eventCardRoot.lookup("#eventImage");
            System.out.println("🔁 lookup eventImage: " + (eventImage != null));
        }

        if (clubLogo == null) {
            clubLogo = (ImageView) eventCardRoot.lookup("#clubLogo");
            System.out.println("🔁 lookup clubLogo: " + (clubLogo != null));
        }

        if (eventFullLabel == null) {
            eventFullLabel = (Label) eventCardRoot.lookup("#eventFullLabel");
            System.out.println("🔁 lookup eventFullLabel: " + (eventFullLabel != null));
        }

    }

    /**
     * Validate that all required components are available
     */
    private boolean validateComponents() {
        boolean allValid = true;

        if (eventName == null) {
            System.out.println("❌ eventName is null");
            allValid = false;
        }
        if (eventDateText == null) {
            System.out.println("❌ eventDateText is null");
            allValid = false;
        }
        if (clubName == null) {
            System.out.println("❌ clubName is null");
            allValid = false;
        }
        if (participantBar == null) {
            System.out.println("❌ participantBar is null");
            allValid = false;
        }
        if (currentParticipantsText == null) {
            System.out.println("❌ currentParticipantsText is null");
            allValid = false;
        }
        if (maxParticipantsText == null) {
            System.out.println("❌ maxParticipantsText is null");
            allValid = false;
        }

        return allValid;
    }

    /**
     * Populate event details (name, club name)
     */
    private void populateEventDetails(Map<String, Object> data) {
        // Set event name
        String eventNameStr = (String) data.get("name");
        if (eventNameStr != null) {
            eventName.setText(eventNameStr);
        } else {
            eventName.setText("Unnamed Event");
        }

        // Set club name
        String clubNameStr = (String) data.get("clubName");
        if (clubNameStr != null && !clubNameStr.isEmpty()) {
            clubName.setText(clubNameStr);
        } else {
            clubName.setText("Unknown Club");
        }

        // Set event date
        Timestamp eventDate = (Timestamp) data.get("eventDate");
        if (eventDate != null && eventDateText != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                .withZone(ZoneId.systemDefault());
            eventDateText.setText(formatter.format(eventDate.toDate().toInstant()));
        } else {
            eventDateText.setText("Date unavailable");
        }
    }

    /**
     * Populate participant information and progress bar
     */
    private void populateParticipantInfo(Map<String, Object> data) {
        int current = 0, min = 0, max = 100;
        try {
            Object o;
            if (data.get("participants") instanceof java.util.List) {
                current = ((java.util.List<?>) data.get("participants")).size();
            } else if ((o = data.get("currentParticipants")) != null) {
                current = ((Number)o).intValue();
            }
            if ((o = data.get("minParticipants")) != null) min = ((Number)o).intValue();
            if ((o = data.get("maxParticipants")) != null) max = ((Number)o).intValue();
        } catch (Exception e) {
            System.out.println("⚠️ Error parsing participant numbers: " + e.getMessage());
        }

        participantBar.setProgress(max > 0 ? (double) current / max : 0);

        final int finalMin = min;
        final int finalMax = max;
        Platform.runLater(() -> {
            double barWidth = participantBar.getWidth();
            double lineWidth = minParticipantLine.getWidth();
            double lineCenterOffset = lineWidth / 2;

            if (barWidth < 2 || lineWidth < 1) {
                Platform.runLater(() -> {
                    double retryWidth = participantBar.getWidth();
                    double retryLineWidth = minParticipantLine.getWidth();
                    double retryCenterOffset = retryLineWidth / 2;
                    double ratio = finalMax > 0 ? (double) finalMin / finalMax : 0;

                    if (finalMin <= 0 || finalMin >= finalMax) {
                        minParticipantLine.setOpacity(0);
                    } else {
                        double translateX = (retryWidth * ratio) - retryCenterOffset;
                        minParticipantLine.setTranslateX(translateX);
                        minParticipantLine.setOpacity(1);
                    }
                });
            } else {
                double minRatio = finalMax > 0 ? (double) finalMin / finalMax : 0;
                if (finalMin <= 0 || finalMin >= finalMax) {
                    minParticipantLine.setOpacity(0);
                } else {
                    double translateX = (barWidth * minRatio) - lineCenterOffset;
                    minParticipantLine.setTranslateX(translateX);
                    minParticipantLine.setOpacity(1);
                }
            }
        });

        currentParticipantsText.setText(String.valueOf(current));
        maxParticipantsText.setText(String.valueOf(max));

        // Show "Event Full" label if at capacity
        boolean isFull = current >= max;
        if (eventFullLabel != null) {
            eventFullLabel.setVisible(isFull);
            eventFullLabel.setManaged(isFull);
        }
    }
    /**
     * Position the red minimum participant line on the progress bar
     */
    private void positionMinParticipantLine(int min, int max) {
        if (max > 0 && min <= max && minParticipantLine != null && participantBar != null) {
            Platform.runLater(() -> {
                try {
                    // Force layout calculation
                    participantBar.applyCss();
                    participantBar.layout();

                    double barWidth = participantBar.getWidth();
                    if (barWidth > 0) {
                        double ratio = (double) min / max;
                        double lineCenterOffset = minParticipantLine.getWidth() / 2;
                        double translateX = (barWidth * ratio) - lineCenterOffset;
                        minParticipantLine.setTranslateX(translateX);
                        minParticipantLine.setOpacity(1);
                    }
                } catch (Exception e) {
                    System.out.println("❌ Error positioning min participant line: " + e.getMessage());
                }
            });
        } else {
            System.out.println("❌ Cannot position min line - invalid parameters or null components");
        }
    }
 

    /**
     * Load event and club images
     */
  private void loadImages(Map<String, Object> data) {
    // ==== 1) Event poster ====
    String posterUrl = (String) data.get("posterUrl");
    if (posterUrl != null && !posterUrl.isEmpty()) {
        try {
            System.out.println("🔍 Loading event image: " + posterUrl);
            eventImage.setImage(new Image(posterUrl, true));
        } catch (Exception e) {
            System.out.println("⚠️ Error loading event image: " + e.getMessage());
        }
    }

    // ==== 2) Kulüp logosu ====
    // 2.a) data içindeki logoUrl
    String logoUrl = (String) data.get("logoUrl");
    System.out.println("🔍 logoUrl from data: " + logoUrl);

    // 2.b) Eğer data'da yoksa Firestore'dan çek
    if ((logoUrl == null || logoUrl.isEmpty()) && data.containsKey("clubId")) {
        String clubId = (String) data.get("clubId");
        System.out.println("🔍 Fallback fetching logo for clubId: " + clubId);
        try {
            DocumentSnapshot clubDoc = FirestoreClient.getFirestore()
                .collection("clubs")
                .document(clubId)
                .get()
                .get();
            if (clubDoc.exists()) {
                logoUrl = clubDoc.getString("logoUrl");
                System.out.println("🔍 logoUrl from Firestore: " + logoUrl);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Error fetching logoUrl from Firestore: " + e.getMessage());
        }
    }

    // 2.c) Gerçekten bir URL varsa yüklüyoruz
    if (logoUrl != null && !logoUrl.isEmpty()) {
        try {
            System.out.println("🔍 Setting clubLogo image: " + logoUrl);
            clubLogo.setImage(new Image(logoUrl, true));
        } catch (Exception e) {
            System.out.println("⚠️ Error loading club logo image: " + e.getMessage());
        }
    } else {
        System.out.println("❌ No valid logoUrl found, clubLogo remains empty.");
    }
} 

    /**
     * Load club logo from Firestore
     */
    private void loadClubLogo(String clubId) {
        try {
            DocumentSnapshot clubDoc = FirestoreClient.getFirestore()
                .collection("clubs")
                .document(clubId)
                .get()
                .get();

            if (clubDoc.exists()) {
                String clubLogoUrl = (String) clubDoc.get("logoUrl");
                if (clubLogoUrl != null && !clubLogoUrl.isEmpty()) {
                    clubLogo.setImage(new Image(clubLogoUrl, true));
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Error loading club logo: " + e.getMessage());
        }
    }

    /**
     * Navigate to event detail page
     */
 private void navigateToEventDetail() {
    if (currentUser == null) {
        System.err.println("❌ [EventCardController] Kullanıcı null, event detayına geçilemiyor!");
        return;
    }

    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_detail.fxml"));
        Parent root = loader.load();

        // EventDetailController'a kullanıcı bilgisini aktar
        EventDetailController controller = loader.getController();
        controller.setLoggedInUser(currentUser);        // HER ZAMAN USERMODELİ SET ET
        controller.setEventData(eventId, eventData); 
        if (currentUser != null) {
            controller.setLoggedInUser(currentUser);
        } else {
            System.err.println("❌ currentUser null, EventDetailController'a gönderilemedi!");
            return; 
        }
        
        controller.setEventData(eventId, eventData);

        Stage stage = (Stage) eventCardRoot.getScene().getWindow();
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    /**
     * Navigates to the detailed event screen when the "Details" button is clicked.
     * NOTE: This method is kept for compatibility, but the new design uses card click instead
     *
     * @param event The ActionEvent triggered by the button click
     */
    @FXML
    private void handleDetails(ActionEvent event) {
        navigateToEventDetail();
    }
    
}