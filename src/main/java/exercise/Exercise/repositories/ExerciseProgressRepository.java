package exercise.Exercise.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exercise.Exercise.entities.ExerciseProgress;

@Repository
public interface ExerciseProgressRepository extends JpaRepository<ExerciseProgress, Long> {

  public List<ExerciseProgress> findByUserId(Long userId);

  Optional<ExerciseProgress> findByUserIdAndExerciseId(Long userId, Long exerciseId);

  boolean existsByUserIdAndExerciseId(Long userId, Long exerciseId);
}