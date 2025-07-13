package exercise.Exercise.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exercise.Exercise.entities.ExerciseVideo;

@Repository
public interface ExerciseVideoRepository extends JpaRepository<ExerciseVideo, Long> {
}