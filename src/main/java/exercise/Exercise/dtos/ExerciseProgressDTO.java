package exercise.Exercise.dtos;

import java.sql.Timestamp;

import exercise.Exercise.entities.Achievement;
import exercise.Exercise.entities.ExerciseProgress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseProgressDTO {

  private Long id;

  private Long userId;

  private ExerciseDTO exerciseDTO;

  private Integer progressRatio;

  private Timestamp createdAt;
  private Timestamp updatedAt;

  public ExerciseProgressDTO(ExerciseProgress exerciseProgress) {
    this.id = exerciseProgress.getId();
    this.userId = exerciseProgress.getUser().getId();
    this.exerciseDTO = new ExerciseDTO(exerciseProgress.getExercise());
    this.progressRatio = exerciseProgress.getProgressRatio();
    this.createdAt = exerciseProgress.getCreatedAt();
    this.updatedAt = exerciseProgress.getUpdatedAt();
  }
}
