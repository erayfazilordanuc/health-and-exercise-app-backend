package exercise.Notification;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import exercise.Notification.entities.FCMTokenDTO;
import exercise.Notification.services.NotificationService;
import exercise.User.entities.User;

@RestController
@RequestMapping("api/notifications")
@Tags(value = @Tag(name = "Notification Operations"))
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/user/fcm-token")
    public ResponseEntity<?> createFcmToken(@RequestBody FCMTokenDTO token,
            @AuthenticationPrincipal User user) {
        if (!token.getUserId().equals(user.getId()))
            throw new BadCredentialsException("You can not create fcm token for someone else");
        return notificationService.createFCMToken(token);
    }
}
