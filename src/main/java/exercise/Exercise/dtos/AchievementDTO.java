package exercise.Exercise.dtos;

import java.sql.Timestamp;

import exercise.Exercise.entities.Achievement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AchievementDTO {

  private Long id;

  private Long userId;

  private Long exerciseId;

  private Timestamp createdAt;

  public AchievementDTO(Achievement achievement) {
    this.id = achievement.getId();
    this.userId = achievement.getUser().getId();
    this.exerciseId = achievement.getExercise().getId();
    this.createdAt = achievement.getCreatedAt();
  }
}
