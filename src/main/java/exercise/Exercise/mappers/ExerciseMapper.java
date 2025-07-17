package exercise.Exercise.mappers;

import java.util.List;

import org.springframework.stereotype.Component;

import exercise.Exercise.dtos.CreateExerciseDTO;
import exercise.Exercise.dtos.ExerciseDTO;
import exercise.Exercise.dtos.ExerciseVideoDTO;
import exercise.Exercise.entities.Exercise;
import exercise.Exercise.mappers.ExerciseMapper;

@Component
public class ExerciseMapper {

  public ExerciseDTO entityToDto(Exercise exercise) {
    List<ExerciseVideoDTO> videoDTOs = exercise.getVideos()
        .stream()
        .map(v -> new ExerciseVideoDTO(
            v.getId(),
            v.getVideoUrl(),
            exercise.getId(),
            v.getCreatedAt()))
        .toList();
    return new ExerciseDTO(
        exercise.getId(),
        exercise.getName(),
        exercise.getDescription(),
        exercise.getPoint(),
        videoDTOs,
        exercise.getAdmin().getId(),
        exercise.getCreatedAt(),
        exercise.getUpdatedAt());
  }
}