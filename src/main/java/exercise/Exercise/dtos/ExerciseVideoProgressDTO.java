package exercise.Exercise.dtos;

import java.math.BigDecimal;
import java.sql.Timestamp;

import exercise.Exercise.entities.ExerciseVideoProgress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseVideoProgressDTO {

  private Long id;

  private BigDecimal progressDuration;
  private Boolean isCompeleted;

  private Long videoId;
  private Long exerciseId;
  private Long userId;

  private Timestamp createdAt;
  private Timestamp updatedAt;

  public ExerciseVideoProgressDTO(ExerciseVideoProgress exerciseVideoProgress) {
    this.id = exerciseVideoProgress.getId();
    this.progressDuration = exerciseVideoProgress.getProgressDuration();
    this.isCompeleted = exerciseVideoProgress.getIsCompeleted();
    this.videoId = exerciseVideoProgress.getVideo().getId();
    this.exerciseId = exerciseVideoProgress.getExercise().getId();
    this.userId = exerciseVideoProgress.getUser().getId();
    this.createdAt = exerciseVideoProgress.getCreatedAt();
    this.updatedAt = exerciseVideoProgress.getUpdatedAt();
  }
}