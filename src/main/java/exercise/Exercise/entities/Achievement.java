package exercise.Exercise.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import exercise.Exercise.dtos.AchievementDTO;
import exercise.User.entities.User;

@Getter
@Setter
@Entity
@Table(name = "achievements")
@AllArgsConstructor
@NoArgsConstructor
public class Achievement {

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

  @CreationTimestamp
  @Column(updatable = false)
  private Timestamp createdAt;
}
