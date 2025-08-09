package exercise.Exercise.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import exercise.User.entities.User;

@Getter
@Setter
@Entity
@Table(name = "exercise_video_progress")
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseVideoProgress {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private BigDecimal progressDuration;

  @Column
  private Boolean isCompeleted = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "video_id")
  @JsonIgnore
  private ExerciseVideo video;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exercise_id")
  @JsonIgnore
  private Exercise exercise;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User user;

  @CreationTimestamp
  @Column(updatable = false)
  private Timestamp createdAt;

  @UpdateTimestamp
  @Column
  private Timestamp updatedAt;

  public ExerciseVideoProgress(User user, Exercise exercise, ExerciseVideo video) {
    this.user = user;
    this.exercise = exercise;
    this.video = video;
  }
}
