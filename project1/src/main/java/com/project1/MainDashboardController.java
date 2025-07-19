package com.project1;

import com.project1.UserModel;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
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
import com.project1.SceneChanger;
import javafx.event.ActionEvent;

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
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch(null));
         Platform.runLater(() -> {
            Stage stage = (Stage) mainEventContainer.getScene().getWindow();
            stage.setMaximized(true);
            // eğer gerçekten tam ekran istersen:
            // stage.setFullScreen(true);
        }); 
    }
    private UserModel loggedInUser;

   @FXML
private void handleSearch(ActionEvent event) {
    String keyword = searchField.getText().trim().toLowerCase();
    System.out.println("🔎 Search: " + keyword);
    mainEventContainer.getChildren().clear();

    try {
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = com.google.firebase.cloud.FirestoreClient
                .getFirestore()
                .collection("events")
                .get()
                .get()
                .getDocuments();

        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : documents) {
            String name = doc.getString("name");
            String desc = doc.getString("description");
            String clubName = doc.getString("clubName");  

            com.google.cloud.Timestamp ts = doc.getTimestamp("eventDate");
            if (ts != null && ts.toDate().before(new Date())) {
                continue;  // geçmiş eventleri atla
            }

            if ((name != null && name.toLowerCase().contains(keyword)) ||
                (desc != null && desc.toLowerCase().contains(keyword)) || 
                (clubName != null && clubName.toLowerCase().contains(keyword))) {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
                VBox eventCard = loader.load();
                EventCardController controller = loader.getController();
                controller.setCurrentUser(loggedInUser);

                Map<String, Object> data = doc.getData();
                String clubId = (String) data.get("clubId");
                if (clubId != null && !clubId.isEmpty()) {
                    try {
                        com.google.cloud.firestore.DocumentSnapshot clubDoc = com.google.firebase.cloud.FirestoreClient.getFirestore()
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
                        } else {
                            data.put("clubName", "Unknown Club");
                        }
                    } catch (Exception ex) {
                        data.put("clubName", "Unknown Club");
                    }
                } else {
                    data.put("clubName", "Unknown Club");
                }

                controller.setData(doc.getId(), data);
                mainEventContainer.getChildren().add(eventCard);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    /**
     * SignInController’dan geçirilen UserModel’i saklar
     * ve UI’yı günceller (\"Hoş geldin Serra\" gibi).
     */
    public void setLoggedInUser(UserModel user) {
        this.loggedInUser = user;
        System.out.println("❇ setLoggedInUser: clubId=" + user.getClubId() + ", clubName=" + user.getClubName());
        loadEvents();
    }
    private void loadAppLogo() {
        // Load logos from application resources instead of Firebase
        try {
            bilkentLogo.setImage(new Image(
                getClass().getResourceAsStream("/images/bilcall_logo.png")
            ));
            appLogo.setImage(new Image(
                getClass().getResourceAsStream("/images/bilkent_logo.png")
            ));
        } catch (Exception e) {
            System.err.println("❌ Logo yüklenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

  private void loadEvents() {
    System.out.println("🔄 loadEvents() başlıyor...");
    mainEventContainer.getChildren().clear();
    System.out.println("🗑️ Önceki kartlar temizlendi.");

    try {
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = com.google.firebase.cloud.FirestoreClient
                .getFirestore()
                .collection("events")
                .get()
                .get()
                .getDocuments();

        System.out.println("📥 Firestore’dan " + documents.size() + " event belgesi alındı.");

        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : documents) {
            String eventId = doc.getId();
            Map<String, Object> data = doc.getData();
            System.out.println("🎯 İşleniyor: eventId=" + eventId);

            // Show only events whose eventDate has not passed
            com.google.cloud.Timestamp ts = doc.getTimestamp("eventDate");
            if (ts != null && ts.toDate().before(new Date())) {
                System.out.println("⌛ Event date passed, skipping: eventId=" + eventId);
                continue;
            }

            // 1) FXML + Controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
            VBox eventCard = loader.load();
            EventCardController controller = loader.getController();
            controller.setCurrentUser(loggedInUser);
            System.out.println("   🔗 FXML yüklendi ve controller bağlandı.");

            // 2) Kulüp adı & logo
            String clubId = (String) data.get("clubId");
            if (clubId != null && !clubId.isEmpty()) {
                try {
                    com.google.cloud.firestore.DocumentSnapshot clubDoc = com.google.firebase.cloud.FirestoreClient.getFirestore()
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


 
   @FXML
private void onProfileButtonClicked(ActionEvent event) {
    // SceneChanger.switchScene(event, "profile.fxml", controller -> {
    //     if (controller instanceof ProfileController pc) {
    //         pc.setUser(loggedInUser);
    //     }
    // });

        FXMLLoader loader = SceneChanger.switchScene(event, "profile.fxml");
        ProfileController pc = loader.getController();
        pc.setUser(loggedInUser);
}

public void setUser(UserModel user) {

    this.loggedInUser = user;
  
    // Burası önemli: Eventleri yeniden yükle!
    
    loadEvents();
   
}
   
    private void loadAllEvents() {
    eventCardContainer.getChildren().clear();

    try {
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = com.google.firebase.cloud.FirestoreClient
                .getFirestore()
                .collection("events")
                .get()
                .get()
                .getDocuments();

        for (com.google.cloud.firestore.DocumentSnapshot doc : documents) {
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
//         com.google.cloud.firestore.Firestore db = com.google.firebase.cloud.FirestoreClient.getFirestore();

//         // 🔹 Club bilgileri
//         String clubId = "cs_club";
//         com.google.cloud.firestore.DocumentSnapshot clubDoc = db.collection("clubs").document(clubId).get().get();

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
//         com.google.cloud.firestore.DocumentReference docRef = db.collection("events").add(event).get();
//         System.out.println("✅ Dummy event Firestore'a yüklendi: " + docRef.getId());

//     } catch (Exception e) {
//         e.printStackTrace();
//     }
// }


private void uploadDummyEventsToFirestore() {
    try {
        com.google.cloud.firestore.Firestore db = com.google.firebase.cloud.FirestoreClient.getFirestore();
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
            com.google.cloud.firestore.DocumentReference docRef = db
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
