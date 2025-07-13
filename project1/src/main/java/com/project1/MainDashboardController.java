package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
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
import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.cloud.Timestamp;

/**
 * Controller class for the main dashboard accessible by student and club_manager roles.
 * Displays user profile, event list, and allows searching.
 * @author Serra
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
    private ImageView bilkentLogo; 

    @FXML
    private ImageView appLogo; 
    
     @FXML
    private FlowPane mainEventContainer;// VBox yerine FlowPane




    @FXML
    public void initialize() {
       System.out.println("🔥 initialize() çalıştı!");
        //uploadDummyEventsToFirestore(); // sadece ilk test için
        //loadDummyEvents(); // kapalı kalsın
        loadAppLogo(); 
        loadEvents(); 

         Platform.runLater(() -> {
            Stage stage = (Stage) mainEventContainer.getScene().getWindow();
            stage.setMaximized(true);
            // eğer gerçekten tam ekran istersen:
            stage.setFullScreen(true);
        }); 
    }
    private void loadAppLogo() {
    // Firestore referans
    DocumentReference settingsRef = FirestoreClient.getFirestore()
        .collection("settings")
        .document("ui");

    try {
        // Bloklayan okuma
        DocumentSnapshot snap = settingsRef.get().get();
        if (!snap.exists()) {
            System.err.println("⚠️ Ayarlar belgesi bulunamadı: settings/ui");
            return;
        }

        // 1) Bilkent logosu
        String bilkentUrl = snap.getString("bilkentLogoUrl");
        if (bilkentUrl != null && !bilkentUrl.isEmpty()) {
            Platform.runLater(() -> {
                System.out.println("🔍 Bilkent logo yükleniyor: " + bilkentUrl);
                bilkentLogo.setImage(new Image(bilkentUrl, true));
            });
        } else {
            System.out.println("⚠️ bilkentLogoUrl alanı boş.");
        }

        // 2) Uygulama (BilCall) logosu
        String appLogoUrl = snap.getString("appLogoUrl");
        if (appLogoUrl != null && !appLogoUrl.isEmpty()) {
            Platform.runLater(() -> {
                System.out.println("🔍 Uygulama (BilCall) logo yükleniyor: " + appLogoUrl);
                appLogo.setImage(new Image(appLogoUrl, true));
            });
        } else {
            System.out.println("⚠️ appLogoUrl alanı boş.");
        }

    } catch (Exception e) {
        System.err.println("❌ Ayarlar yüklenirken hata: " + e.getMessage());
        e.printStackTrace();
    }
}
    

  private void loadEvents() {
    System.out.println("🔄 loadEvents() başlıyor...");
    mainEventContainer.getChildren().clear();
    System.out.println("🗑️ Önceki kartlar temizlendi.");

    try {
        List<QueryDocumentSnapshot> documents = FirestoreClient
                .getFirestore()
                .collection("events")
                .get()
                .get()
                .getDocuments();

        System.out.println("📥 Firestore’dan " + documents.size() + " event belgesi alındı.");

        for (QueryDocumentSnapshot doc : documents) {
            String eventId = doc.getId();
            Map<String, Object> data = doc.getData();
            System.out.println("🎯 İşleniyor: eventId=" + eventId);

            // 1) FXML + Controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
            VBox eventCard = loader.load();
            EventCardController controller = loader.getController();
            System.out.println("   🔗 FXML yüklendi ve controller bağlandı.");

            // 2) Kulüp adı & logo
            String clubId = (String) data.get("clubId");
            if (clubId != null && !clubId.isEmpty()) {
                try {
                    DocumentSnapshot clubDoc = FirestoreClient.getFirestore()
                            .collection("clubs")
                            .document(clubId)
                            .get()
                            .get();
                    if (clubDoc.exists()) {
                        String cname = clubDoc.getString("name");
                        data.put("clubName", cname != null ? cname : "Unknown Club");
                        String logoUrl = clubDoc.getString("logoUrl");
                        if (logoUrl != null && !logoUrl.isEmpty()) {
                            data.put("logoUrl", logoUrl);
                        }
                        System.out.println("   🏷️ clubName ve logoUrl ayarlandı.");
                    } else {
                        data.put("clubName", "Unknown Club");
                        System.out.println("   ⚠️ clubDoc bulunamadı, fallback olarak Unknown Club.");
                    }
                } catch (Exception ex) {
                    data.put("clubName", "Unknown Club");
                    System.out.println("   ❌ Club bilgisi alınamadı:");
                    ex.printStackTrace();
                }
            } else {
                data.put("clubName", "Unknown Club");
                System.out.println("   ⚠️ Event içinde clubId yok, fallback olarak Unknown Club.");
            }

            // 3) Controller’a veri verme
            controller.setData(eventId, data);
            System.out.println("   ✅ controller.setData() çağrıldı.");

            // 4) FlowPane’e ekleme
            mainEventContainer.getChildren().add(eventCard);
            System.out.println("   ➕ Event kartı FlowPane’e eklendi.");
        }

        System.out.println("🔄 loadEvents() tamamlandı.");
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
//    private void uploadDummyEventToFirestore() {
//     try {
//         Firestore db = FirestoreClient.getFirestore();

//         // 🔹 Club bilgileri
//         String clubId = "cs_club";
//         DocumentSnapshot clubDoc = db.collection("clubs").document(clubId).get().get();

//         String clubName;
//         if (!clubDoc.exists()) {
//             // 🔸 Eğer kulüp yoksa, yeni kulüp belgesi oluştur
//             Map<String, Object> club = new HashMap<>();
//             clubName = "Computer Science Club";
//             club.put("name", clubName);
//             club.put("logoUrl", "https://via.placeholder.com/60x60.png");

//             db.collection("clubs").document(clubId).set(club);
//             System.out.println("✅ Dummy club 'cs_club' Firestore'a yüklendi.");
//         } else {
//             // 🔸 Kulüp varsa ismini al
//             clubName = (String) clubDoc.get("name");
//         }

//         // 🔹 Şu anki zamanı al
//         Timestamp now = Timestamp.now();

//         // 🔹 Dummy event bilgileri
//         Map<String, Object> event = new HashMap<>();
//         event.put("name", "Tech Talk 2025");
//         event.put("description", "AI and the Future of Computing");
//         event.put("eventType", "event");
//         event.put("clubId", clubId);
//         event.put("clubName", clubName); // 👈 Artık her zaman vardır
//         event.put("timestamp", now);
//         event.put("eventDate", now);
//         event.put("location", "Bilkent EE-01");
//         event.put("minParticipants", 10);
//         event.put("maxParticipants", 100);
//         event.put("currentParticipants", 4);
//         event.put("posterUrl", "https://via.placeholder.com/400x180.png");

//         // 🔹 Event Firestore’a ekleniyor
//         DocumentReference docRef = db.collection("events").add(event).get();
//         System.out.println("✅ Dummy event Firestore'a yüklendi: " + docRef.getId());

//     } catch (Exception e) {
//         e.printStackTrace();
//     }
// }


private void uploadDummyEventsToFirestore() {
    try {
        Firestore db = FirestoreClient.getFirestore();
        Timestamp now = Timestamp.now();

        // 🔹 Birden fazla dummy event bilgisi
        List<Map<String, Object>> events = Arrays.asList(
            new HashMap<String, Object>() {{
                put("name", "Calculus Workshop");
                put("description", "Multiple Integrals & Applications");
                put("eventType", "workshop");
                put("clubId", "math_club");
                put("clubName", "Mathematics Club");
                put("timestamp", now);
                put("eventDate", new Date(2025 - 1900, Calendar.JULY, 20, 15, 0, 0));
                put("location", "Bilkent MA-02");
                put("minParticipants", 10);
                put("maxParticipants", 50);
                put("currentParticipants", 12);
                put("posterUrl", "https://via.placeholder.com/400x180.png");
            }},
            new HashMap<String, Object>() {{
                put("name", "Summer Hackathon");
                put("description", "Build your first full-stack app in 24h");
                put("eventType", "hackathon");
                put("clubId", "hack_club");
                put("clubName", "Hackathon Club");
                put("timestamp", now);
                put("eventDate", new Date(2025 - 1900, Calendar.AUGUST, 5, 9, 30, 0));
                put("location", "Bilkent CC-10");
                put("minParticipants", 5);
                put("maxParticipants", 80);
                put("currentParticipants", 40);
                put("posterUrl", "https://via.placeholder.com/400x180.png");
            }}
        );

        // 🔹 Her bir event’i Firestore’a ekle
        for (Map<String, Object> event : events) {
            DocumentReference docRef = db
                .collection("events")
                .add(event)
                .get();  // Bekle ve referansı al
            System.out.println("✅ Dummy event yüklendi: " + docRef.getId());
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    
}
