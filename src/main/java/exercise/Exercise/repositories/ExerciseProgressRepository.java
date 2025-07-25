package exercise.Exercise.repositories;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exercise.Exercise.entities.ExerciseProgress;

@Repository
public interface ExerciseProgressRepository extends JpaRepository<ExerciseProgress, Long> {

  public List<ExerciseProgress> findByUserId(Long userId);

  ExerciseProgress findByUserIdAndExerciseIdAndCreatedAtBetween(
      Long userId,
      Long exerciseId,
      Timestamp startOfDay,
      Timestamp endOfDay);

  boolean existsByUserIdAndExerciseId(Long userId, Long exerciseId);
}