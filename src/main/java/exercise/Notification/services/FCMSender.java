package exercise.Notification.services;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

@Service
public class FCMSender {

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
}
