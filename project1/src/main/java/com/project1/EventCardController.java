package com.project1;

import javafx.scene.layout.StackPane;
// width listener’ı tutmak için
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
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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
    private Label eventName;

    @FXML 
    private Label eventDateText;

    // REMOVED: eventDateText is no longer in the design
    // @FXML
    // private Label eventDateText;

    @FXML 
    private Label clubName;

    @FXML 
    private ImageView eventImage;

    @FXML 
    private ImageView clubLogo;




    /**
     * Event card loading whether based on clicked or not
     * @author Serra
     */
    @FXML
    private void onCardClicked(MouseEvent event) {
        System.out.println("📦 Event card clicked!");
        // Navigate to event detail page
        if (eventId != null && eventData != null) {
            navigateToEventDetail();
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

        // Set event title and organizing club name
        populateEventDetails(data);
        
        // Set participant information and progress bar
        populateParticipantInfo(data);

        // Load images
        loadImages(data);
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
  /**
 * Populate participant information and progress bar
 */
private void populateParticipantInfo(Map<String, Object> data) {
    // 1) Katılımcı sayıları
    int current = 0, min = 0, max = 100;
    try {
        Object o;
        if ((o = data.get("currentParticipants")) != null) current = ((Number)o).intValue();
        if ((o = data.get("minParticipants"))     != null) min     = ((Number)o).intValue();
        if ((o = data.get("maxParticipants"))     != null) max     = ((Number)o).intValue();
    } catch (Exception e) {
        System.out.println("⚠️ Error parsing participant numbers: " + e.getMessage());
    }

    System.out.printf("🔢 Participants - current: %d, min: %d, max: %d%n", current, min, max);

    // 2) ProgressBar’ı ayarla
    double progress = max > 0 ? (double) current / max : 0;
    participantBar.setProgress(progress);

    // 3) Metinleri güncelle
    currentParticipantsText.setText(String.valueOf(current));
    maxParticipantsText.setText(String.valueOf(max));

    // 4) Önceki listener’i temizle (eğer daha önce eklenmişse)
    if (widthListener != null) {
        participantBar.widthProperty().removeListener(widthListener);
    }

    // 5) Listener’ı tanımla
    final double ratio = (max > 0 ? (double) min / max : 0);
    widthListener = (obs, oldW, newW) -> {
        double barWidth = newW.doubleValue();
        // Çizginin x konumu = çubuğun solundan barWidth*ratio
        minParticipantLine.setTranslateX(barWidth * ratio);
    };

    // 6) Hemen bir kez çalıştır
    widthListener.changed(null, 0.0, participantBar.getWidth());

    // 7) Dinleme başladığında hem ilk boyut hem de sonradan değişimler için
    participantBar.widthProperty().addListener(widthListener);
}

    /**
     * Position the red minimum participant line on the progress bar
     */
    private void positionMinParticipantLine(int min, int max) {
        if (max > 0 && min <= max && minParticipantLine != null && participantBar != null) {
            javafx.application.Platform.runLater(() -> {
                try {
                    // Force layout calculation
                    participantBar.applyCss();
                    participantBar.layout();

                    double barWidth = participantBar.getWidth();
                    System.out.println("📏 participantBar width: " + barWidth);

                    if (barWidth > 0) {
                        double ratio = (double) min / max;
                        double lineCenterOffset = minParticipantLine.getWidth() / 2;
                        double translateX = (barWidth * ratio) - lineCenterOffset;

                        System.out.println("📐 Min line translateX: " + translateX + " (ratio: " + ratio + ")");
                        
                        minParticipantLine.setTranslateX(translateX);
                    } else {
                        System.out.println("⚠️ Progress bar width is 0, will retry positioning");
                        // Retry after a short delay
                        javafx.application.Platform.runLater(() -> {
                            double retryWidth = participantBar.getWidth();
                            if (retryWidth > 0) {
                                double ratio = (double) min / max;
                                double lineCenterOffset = minParticipantLine.getWidth() / 2;
                                double translateX = (retryWidth * ratio) - lineCenterOffset;
                                minParticipantLine.setTranslateX(translateX);
                            }
                        });
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

    // 2.b) Eğer data’da yoksa Firestore’dan çek
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_detail.fxml"));
            Parent root = loader.load();

            EventDetailController controller = loader.getController();
            controller.setEventData(eventId, eventData);

            // Get current stage from any component
            Stage stage = (Stage) eventCardRoot.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            System.out.println("❌ Error navigating to event detail: " + e.getMessage());
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