package exercise.Common.services;

import java.io.FileInputStream;

import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Service
public class FirebaseInitializeService {

  @PostConstruct
  public void initialize() {
    try {
      FileInputStream serviceAccount = new FileInputStream("./serviceAccountKey.json");

      FirebaseOptions options = new FirebaseOptions.Builder()
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .build();

      FirebaseApp.initializeApp(options);

    } catch (Exception e) {
      // TODO: handle exception
    }
  }
}
