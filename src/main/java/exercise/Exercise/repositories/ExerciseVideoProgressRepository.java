package exercise.Exercise.repositories;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exercise.Exercise.entities.ExerciseVideoProgress;

@Repository
public interface ExerciseVideoProgressRepository extends JpaRepository<ExerciseVideoProgress, Long> {

    public List<ExerciseVideoProgress> findByUserId(Long userId);

    List<ExerciseVideoProgress> findByUserIdAndCreatedAtBetween(
            Long userId,
            Timestamp startOfDay,
            Timestamp endOfDay);

    Optional<ExerciseVideoProgress> findByUserIdAndVideoIdAndCreatedAtBetween(
            Long userId,
            Long videoId,
            Timestamp startOfDay,
            Timestamp endOfDay);

    boolean existsByUserIdAndExerciseId(Long userId, Long exerciseId);
}