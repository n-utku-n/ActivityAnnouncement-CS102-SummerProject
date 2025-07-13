🎓 ActivityAnnouncement-CS102-SummerProject

Bu proje, Bilkent Üniversitesi kulüplerinin etkinliklerini öğrencilere duyurmak için geliştirilen bir masaüstü uygulamasıdır. Java, JavaFX ve Firebase teknolojileri kullanılarak tasarlanmıştır.

------------------------------------------------------------
🚀 Kurulum

1. Bu repoyu klonlayın:

   git clone https://github.com/n-utku-n/ActivityAnnouncement-CS102-SummerProject.git
   cd ActivityAnnouncement-CS102-SummerProject

2. Gerekli araçları yükleyin:
   - JDK 17+
   - Maven
   - JavaFX SDK
   - Firebase Admin SDK (sadece backend için)

------------------------------------------------------------
🔐 Firebase Bağlantısı

Uygulamanın çalışması için `serviceAccountKey.json` dosyasına ihtiyacınız vardır.

Lütfen aşağıdaki iki yöntemden birini izleyin:

1. Firebase konsolunda bir proje oluşturun.
2. Sol menüden “Proje Ayarları” → “Hizmet Hesapları” sekmesine gidin.
3. “Yeni özel anahtar oluştur” butonuna tıklayın.
4. `serviceAccountKey.json` dosyasını indirin.
5. Aşağıdaki konuma yapıştırın:

   project1/src/main/resources/firebase/serviceAccountKey.json

   # YA DA

   Utku'nun sizinle paylaştığı dosyayı belirtilen adla belirtilen yere yapıştırın (sorun olduğunu düşündüğünüz zaman tekrar isteyebilirsiniz):
      project1/src/main/resources/firebase/serviceAccountKey.json


Not: Bu dosya .gitignore içine alınmıştır ve ASLA GitHub’a yüklenmemelidir.

------------------------------------------------------------
⚠️ UYARI: Bu projeyi daha önce klonladıysanız...

Bu repository'nin geçmişinde gizli anahtar dosyası (`serviceAccountKey.json`) bulunuyordu.  
Tüm geçmiş temizlendi, ancak eski bir kopyanız varsa:

Güvenliğiniz için:

   git fetch origin
   git reset --hard origin/main

YA DA:

   # En temizi:
   rm -rf eski_klasör
   git clone https://github.com/n-utku-n/ActivityAnnouncement-CS102-SummerProject.git

------------------------------------------------------------
📦 Katkı ve Geliştirme

Her türlü katkıya açığız! PR göndermeden önce lütfen bir issue açın.

------------------------------------------------------------
🛡 Güvenlik

Bu projede GitHub Secret Scanning özelliği aktiftir. Gizli anahtar içeren commit’ler otomatik olarak taranır.

------------------------------------------------------------
📄 Lisans

None


# -----------English-----------
🎓 ActivityAnnouncement-CS102-SummerProject

This project is a desktop application designed to help Bilkent University clubs announce their events to students. It is developed using Java, JavaFX, and Firebase technologies.

------------------------------------------------------------
🚀 Setup

1. Clone this repository:

   git clone https://github.com/n-utku-n/ActivityAnnouncement-CS102-SummerProject.git
   cd ActivityAnnouncement-CS102-SummerProject

2. Install the required tools:
   - JDK 17+
   - Maven
   - JavaFX SDK
   - Firebase Admin SDK (for backend use)

------------------------------------------------------------
🔐 Firebase Integration

To run the project, you need a `serviceAccountKey.json` file.

Please follow these steps:

1. Go to [Firebase Console](https://console.firebase.google.com/) and create a project.
2. Navigate to "Project Settings" → "Service Accounts" tab.
3. Click “Generate new private key”.
4. Download the `serviceAccountKey.json` file.
5. Place the file in the following directory:

   project1/src/main/resources/firebase/serviceAccountKey.json

    # OR

   Copy the file that Utku shared with you with the specified name to the specified location (you can request it again if you think there is a problem):
      project1/src/main/resources/firebase/serviceAccountKey.json

Note: This file is listed in `.gitignore` and **must never be uploaded to GitHub**.

------------------------------------------------------------
⚠️ WARNING: If you cloned this repo earlier...

This repository previously included a private Firebase key (`serviceAccountKey.json`) in its history.  
The history has been fully rewritten, but if you have an old copy:

To avoid security issues:

   git fetch origin
   git reset --hard origin/main

OR, the safest way:

   Delete the old folder and re-clone the repository:

   git clone https://github.com/n-utku-n/ActivityAnnouncement-CS102-SummerProject.git

------------------------------------------------------------
📦 Contributing

All contributions are welcome! Please open an issue before submitting a pull request.

------------------------------------------------------------
🛡 Security

GitHub Secret Scanning is enabled for this repository. Commits containing sensitive information will be automatically flagged.

------------------------------------------------------------
📄 License

None
