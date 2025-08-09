package exercise.Exercise.dtos;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import exercise.Exercise.entities.Exercise;
import exercise.Exercise.entities.ExerciseVideo;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseVideoDTO {
  private Long id;

  private String name;

  private String videoUrl;

  private Integer durationSeconds;

  private Long exerciseId;

  private Timestamp createdAt;

  public ExerciseVideoDTO(ExerciseVideo video) {
    this.id = video.getId();
    this.name = video.getName();
    this.videoUrl = video.getVideoUrl();
    this.durationSeconds = video.getDurationSeconds();
    this.exerciseId = video.getExercise().getId();
    this.createdAt = video.getCreatedAt();
  }
}
