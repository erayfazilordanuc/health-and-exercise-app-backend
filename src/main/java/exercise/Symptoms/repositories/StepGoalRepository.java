package exercise.Symptoms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exercise.Symptoms.entities.StepGoal;

@Repository
public interface StepGoalRepository extends JpaRepository<StepGoal, Long> {

  public StepGoal findByUserId(Long userId);
}