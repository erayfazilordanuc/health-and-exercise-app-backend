package exercise.Exercise.mappers;

import org.springframework.stereotype.Component;

import exercise.Exercise.dtos.CreateExerciseDTO;
import exercise.Exercise.dtos.ExerciseDTO;
import exercise.Exercise.entities.Exercise;
import exercise.Exercise.mappers.ExerciseMapper;

@Component
public class ExerciseMapper {

  public ExerciseDTO entityToDto(Exercise exercise) {
    return new ExerciseDTO(
        exercise.getId(),
        exercise.getName(),
        exercise.getDescription(),
        exercise.getPoint(),
        exercise.getVideos(),
        exercise.getAdmin().getId(),
        exercise.getCreatedAt(),
        exercise.getUpdatedAt());
  }
}