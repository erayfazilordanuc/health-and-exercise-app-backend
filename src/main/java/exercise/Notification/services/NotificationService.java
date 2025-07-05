package exercise.Notification.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import exercise.Notification.entities.FCMToken;
import exercise.Notification.entities.FCMTokenDTO;
import exercise.Notification.repositories.FCMTokenRepository;

@Service
public class NotificationService {

  @Autowired
  private FCMTokenRepository fcmTokenRepo;

  @Autowired
  private FCMSender fcmSender;

  public ResponseEntity<?> createFCMToken(FCMTokenDTO tokenDTO) {
    boolean isValid = fcmSender.testSend(tokenDTO.getToken());

    if (!isValid) {
      return ResponseEntity.badRequest().body("Invalid FCM token");
    }

    FCMToken token = new FCMToken(tokenDTO);

    fcmTokenRepo.save(token);

    return ResponseEntity.ok("FCM token updated");
  }
}