package exercise.Notification.entities;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FCMTokenDTO {
  private Long id;

  private Long userId;

  private String token;

  private String platform;

  private LocalDateTime createdAt;
}
