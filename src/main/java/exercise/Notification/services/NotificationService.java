package exercise.Notification.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import exercise.Notification.dtos.NotificationDTO;
import exercise.Notification.entities.FCMToken;
import exercise.Notification.entities.FCMTokenDTO;
import exercise.Notification.repositories.FCMTokenRepository;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;

@Service
public class NotificationService {

  @Autowired
  private FCMTokenRepository fcmTokenRepo;

  @Autowired
  private UserRepository userRepo;

  public ResponseEntity<?> createFCMToken(FCMTokenDTO tokenDTO) {
    boolean isValid = testSend(tokenDTO.getToken());

    if (!isValid) {
      return ResponseEntity.badRequest().body("Invalid FCM token");
    }

    FCMToken token = new FCMToken(tokenDTO);

    fcmTokenRepo.save(token);

    return ResponseEntity.ok("FCM token updated");
  }

  public boolean testSend(String token) {
    try {
      Message message = Message.builder()
          .setToken(token)
          .putData("test", "ping")
          .build();
      String response = FirebaseMessaging.getInstance().send(message);
      return true;
    } catch (FirebaseMessagingException e) {
      if (e.getErrorCode().equals("registration-token-not-registered")
          || e.getErrorCode().equals("invalid-argument")) {
        return false;
      }
      throw new RuntimeException("FCM error: " + e.getMessage(), e);
    }
  }

  public ResponseEntity<?> sendNotification(NotificationDTO notificationDTO) {
    User user = userRepo.findByUsername(notificationDTO.getReceiver());
    FCMToken fcmToken = fcmTokenRepo.findByUserId(user.getId());

    try {
      Message message = Message.builder()
          .setToken(fcmToken.getToken())
          .setNotification(Notification.builder()
              .setTitle(user.getFullName() + "")
              .setBody(notificationDTO.getMessage())
              .build())
          // .putData("extraKey", "extraValue") // opsiyonel data payload
          .build();

      String response = FirebaseMessaging.getInstance().send(message);
      System.out.println("✅ Successfully sent message: " + response);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("❌ Error sending FCM message", e);
    }

    return ResponseEntity.ok("Notification sent");
  }
}