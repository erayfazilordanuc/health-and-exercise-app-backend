package exercise.Symptoms.repositories; // Adjust package name as needed

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository; // Import Timestamp
import org.springframework.stereotype.Repository;

import exercise.Symptoms.entities.StepGoal;

@Repository
public interface StepGoalRepository extends JpaRepository<StepGoal, Long> {

  // --- Basic Finders ---

  List<StepGoal> findByUserId(Long userId);

  Optional<StepGoal> findTopByUserIdOrderByCreatedAtDesc(Long userId);

  List<StepGoal> findAllByUserIdOrderByCreatedAtDesc(Long userId);

  // --- Time-Based Finders (Using Timestamp) ---

  /**
   * Finds the latest StepGoal for a user within a specific time range.
   * 
   * @param userId The user's ID.
   * @param start  The start of the time range (inclusive).
   * @param end    The end of the time range (inclusive).
   * @return An Optional containing the latest StepGoal if found.
   */
  Optional<StepGoal> findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
      Long userId, Timestamp start, Timestamp end); // Changed to Timestamp

  // --- Finders for Completed Goals (Using Timestamp) ---

  /**
   * Finds all completed StepGoals for a user, ordered by creation date
   * descending.
   * 
   * @param userId The user's ID.
   * @return A list of completed StepGoals.
   */
  List<StepGoal> findAllByUserIdAndIsDoneTrueOrderByCreatedAtDesc(Long userId);

  /**
   * Finds all completed StepGoals for a user up to (and including) a specific end
   * date/time.
   * 
   * @param userId  The user's ID.
   * @param endDate The end timestamp (inclusive).
   * @return A list of completed StepGoals up to the specified time.
   */
  List<StepGoal> findAllByUserIdAndIsDoneTrueAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
      Long userId, Timestamp endDate); // Changed to Timestamp

  // Note: The duplicate method accepting LocalDateTime has been removed.
}