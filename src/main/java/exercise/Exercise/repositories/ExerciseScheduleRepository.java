package exercise.Exercise.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exercise.Exercise.entities.ExerciseSchedule;

@Repository
public interface ExerciseScheduleRepository extends JpaRepository<ExerciseSchedule, Long> {
        Optional<ExerciseSchedule> findByUserId(Long userId);
}