package exercise.Exercise.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import exercise.User.entities.User;

@Getter
@Setter
@Entity
@Table(name = "exercise_progress")
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseProgress {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exercise_id")
  @JsonIgnore
  private Exercise exercise;

  @Min(value = 0, message = "Progress ratio must be at least 0")
  @Max(value = 100, message = "Progress ratio cannot exceed 100")
  @Column
  private Integer progressRatio;

  @CreationTimestamp
  @Column(updatable = false)
  private Timestamp createdAt;

  @UpdateTimestamp
  @Column
  private Timestamp updatedAt;
}
