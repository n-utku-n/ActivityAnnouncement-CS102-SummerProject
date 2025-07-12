package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.cloud.Timestamp;

/**
 * Controller class for the main dashboard accessible by student and club_manager roles.
 * Displays user profile, event list, and allows searching.
 * @author Utku
 */
public class MainDashboardController {

    @FXML
    private VBox eventCardContainer;

    @FXML
    private Button profileButton;

    @FXML
    private TextField searchField;

    @FXML
    private GridPane eventGrid;

    @FXML
    private VBox mainEventContainer; // FXML'deki container için

    @FXML
    public void initialize() {
       System.out.println("🔥 initialize() çalıştı!");
        //uploadDummyEventToFirestore(); // sadece ilk test için
        //loadDummyEvents(); // kapalı kalsın
        loadEvents(); 

         Platform.runLater(() -> {
            Stage stage = (Stage) mainEventContainer.getScene().getWindow();
            stage.setMaximized(true);
            // eğer gerçekten tam ekran istersen:
            stage.setFullScreen(true);
        });
        
    }


  private void loadEvents() {
    mainEventContainer.getChildren().clear();

    try {
        List<QueryDocumentSnapshot> documents = FirestoreClient
                .getFirestore()
                .collection("events")
                .get()
                .get()
                .getDocuments();

        for (QueryDocumentSnapshot doc : documents) {
            // 1. FXML ve controller yükleniyor
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
            VBox eventCard = loader.load();
            EventCardController controller = loader.getController();

            // 2. Event verisi alınıyor
            Map<String, Object> data = doc.getData();

            // 3. clubName ve logoUrl çekiliyor (eğer varsa)
            String clubId = (String) data.get("clubId");
            if (clubId != null && !clubId.isEmpty()) {
                try {
                    DocumentSnapshot clubDoc = FirestoreClient.getFirestore()
                            .collection("clubs")
                            .document(clubId)
                            .get()
                            .get();
                    if (clubDoc.exists()) {
                        // kulüp adı
                        String clubName = clubDoc.getString("name");
                        data.put("clubName",
                            clubName != null ? clubName : "Unknown Club");
                        // kulüp logosu
                        String logoUrl = clubDoc.getString("logoUrl");
                        if (logoUrl != null && !logoUrl.isEmpty()) {
                            data.put("logoUrl", logoUrl);
                        }
                    } else {
                        System.out.println("⚠️ clubDoc yok: " + clubId);
                        data.put("clubName", "Unknown Club");
                    }
                } catch (Exception e) {
                    System.out.println("❌ Club bilgisi alınamadı: " + clubId);
                    e.printStackTrace();
                    data.put("clubName", "Unknown Club");
                }
            } else {
                System.out.println("⚠️ Event belgesinde clubId yok veya boş.");
                data.put("clubName", "Unknown Club");
            }

            // 4. Veriler controller'a gönderiliyor
            controller.setData(doc.getId(), data);

            // 5. Ana görünüme ekleniyor
            mainEventContainer.getChildren().add(eventCard);
        }

    } catch (Exception e) {
        System.out.println("❌ Hata: Eventler yüklenemedi");
        e.printStackTrace();
    }
}


    /**
     * TODO: not implemented
     * @author Utku
     */
    @FXML
    private void handleProfile() {
        System.out.println("👤 Profil butonuna tıklandı (gelecekte profil ekranı açılacak).");
    }

    private void loadAllEvents() {
    eventCardContainer.getChildren().clear();

    try {
        List<QueryDocumentSnapshot> documents = FirestoreClient
                .getFirestore()
                .collection("events")
                .get()
                .get()
                .getDocuments();

        for (DocumentSnapshot doc : documents) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
            VBox card = loader.load();

            EventCardController controller = loader.getController();
            controller.setData(doc.getId(), doc.getData());

            eventCardContainer.getChildren().add(card);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

/**
     * TODO: implemented
     * @author Serra
     */
    //firesotre şimdilik örnek event ekleme
   private void uploadDummyEventToFirestore() {
    try {
        Firestore db = FirestoreClient.getFirestore();

        // 🔹 Club bilgileri
        String clubId = "cs_club";
        DocumentSnapshot clubDoc = db.collection("clubs").document(clubId).get().get();

        String clubName;
        if (!clubDoc.exists()) {
            // 🔸 Eğer kulüp yoksa, yeni kulüp belgesi oluştur
            Map<String, Object> club = new HashMap<>();
            clubName = "Computer Science Club";
            club.put("name", clubName);
            club.put("logoUrl", "https://via.placeholder.com/60x60.png");

            db.collection("clubs").document(clubId).set(club);
            System.out.println("✅ Dummy club 'cs_club' Firestore'a yüklendi.");
        } else {
            // 🔸 Kulüp varsa ismini al
            clubName = (String) clubDoc.get("name");
        }

        // 🔹 Şu anki zamanı al
        Timestamp now = Timestamp.now();

        // 🔹 Dummy event bilgileri
        Map<String, Object> event = new HashMap<>();
        event.put("name", "Tech Talk 2025");
        event.put("description", "AI and the Future of Computing");
        event.put("eventType", "event");
        event.put("clubId", clubId);
        event.put("clubName", clubName); // 👈 Artık her zaman vardır
        event.put("timestamp", now);
        event.put("eventDate", now);
        event.put("location", "Bilkent EE-01");
        event.put("minParticipants", 10);
        event.put("maxParticipants", 100);
        event.put("currentParticipants", 4);
        event.put("posterUrl", "https://via.placeholder.com/400x180.png");

        // 🔹 Event Firestore’a ekleniyor
        DocumentReference docRef = db.collection("events").add(event).get();
        System.out.println("✅ Dummy event Firestore'a yüklendi: " + docRef.getId());

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    
}
