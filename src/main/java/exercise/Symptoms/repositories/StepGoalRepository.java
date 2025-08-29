package exercise.Symptoms.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exercise.Symptoms.entities.StepGoal;

@Repository
public interface StepGoalRepository extends JpaRepository<StepGoal, Long> {

  public List<StepGoal> findByUserId(Long userId);

  Optional<StepGoal> findTopByUserIdOrderByCreatedAtDesc(Long userId);

  List<StepGoal> findAllByUserIdOrderByCreatedAtDesc(Long userId);

  Optional<StepGoal> findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
      Long userId, LocalDateTime start, LocalDateTime end);

  List<StepGoal> findAllByUserIdAndIsDoneTrueOrderByCreatedAtDesc(Long userId);
}