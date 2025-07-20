package exercise.Exercise.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exercise.Exercise.entities.Achievement;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

  public List<Achievement> findByUserId(Long userId);

  Optional<Achievement> findByUserIdAndExerciseId(Long userId, Long exerciseId);

  boolean existsByUserIdAndExerciseId(Long userId, Long exerciseId);
}