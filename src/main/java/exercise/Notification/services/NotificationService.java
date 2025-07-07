package exercise.Notification.services;

import java.util.List;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import exercise.Message.services.MessageService;
import exercise.Notification.dtos.NotificationDTO;
import exercise.Notification.entities.DeleteFCMTokenDTO;
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

  @Autowired
  public MessageService messageService;

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

  public ResponseEntity<?> sendNotification(NotificationDTO notificationDTO, User sender) {
    User receiver = userRepo.findByUsername(notificationDTO.getReceiver());
    List<FCMToken> fcmTokens = fcmTokenRepo.findByUserId(receiver.getId());

    Long roomId = messageService.isRoomExistBySenderAndReceiver(sender.getUsername(), receiver.getUsername());

    if (roomId == 0)
      throw new RuntimeException("Room not found");

    fcmTokens.stream().forEach(token -> {
      try {
        Message message = Message.builder()
            .setToken(token.getToken())
            .setNotification(Notification.builder()
                .setTitle(sender.getFullName()/* + " kullanıcısından gelen mesaj" */)
                .setBody(notificationDTO.getMessage())
                .build())
            .putData("screen", "Chat")
            .putData("roomId", roomId.toString())
            .putData("sender", sender.getUsername())
            .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("✅ Successfully sent notification message to token " + token.getToken() + ": " + response);
      } catch (Exception e) {
        System.err.println("❌ Failed to send notification to token " + token.getToken());
        e.printStackTrace();
      }
    });

    return ResponseEntity.ok("Notification sent");
  }

  public ResponseEntity<?> deleteFCMToken(DeleteFCMTokenDTO tokenDTO) {
    List<FCMToken> fcmTokens = fcmTokenRepo.findByUserId(tokenDTO.getUserId());
    List<FCMToken> tokensToDelete = fcmTokens.stream()
        .filter(token -> tokenDTO.getPlatform().equalsIgnoreCase(token.getPlatform()))
        .collect(Collectors.toList());

    fcmTokenRepo.deleteAll(tokensToDelete);
    return ResponseEntity.ok("Tokens deleted");
  }
}