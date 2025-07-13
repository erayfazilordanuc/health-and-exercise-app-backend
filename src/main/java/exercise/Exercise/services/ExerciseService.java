package exercise.Exercise.services;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import exercise.Exercise.dtos.CreateExerciseDTO;
import exercise.Exercise.entities.Exercise;
import exercise.Exercise.repositories.ExerciseRepository;
import exercise.User.dtos.UserDTO;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;

@Service
public class ExerciseService {

  @Autowired
  private ExerciseRepository exerciseRepo;

  @Autowired
  private S3Service s3Service;

  public Exercise create(CreateExerciseDTO exerciseDTO) throws IOException {
    String videoUrl = s3Service.uploadObject(exerciseDTO.getVideoFile(), "videos");

    Exercise newExercise = new Exercise(null, exerciseDTO.getName(), videoUrl,
        exerciseDTO.getPoint(), null, null);

    Exercise savedExercise = exerciseRepo.save(newExercise);
    return savedExercise;
  }
}
