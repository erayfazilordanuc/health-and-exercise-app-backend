package exercise.Exercise.dtos;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import exercise.Exercise.entities.Exercise;
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

  private String videoUrl;

  private Long exerciseId;

  private Timestamp createdAt;
}
