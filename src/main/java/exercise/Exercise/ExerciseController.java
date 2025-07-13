package exercise.Exercise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import exercise.Exercise.dtos.CreateExerciseDTO;
import exercise.Exercise.entities.Exercise;
import exercise.Exercise.repositories.ExerciseRepository;
import exercise.Exercise.services.ExerciseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/exercises")
@Tags(value = @Tag(name = "Exercise Operations"))
public class ExerciseController {

  @Autowired
  private ExerciseRepository exerciseRepository;

  @Autowired
  private ExerciseService exerciseService;

  @PostMapping
  public Exercise createExercise(@RequestBody CreateExerciseDTO exerciseDTO) throws IOException {
    Exercise exercise = exerciseService.create(exerciseDTO);
    return exerciseRepository.save(exercise);
  }

  @GetMapping
  public List<Exercise> getAllExercises() {
    return exerciseRepository.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Exercise> getExerciseById(@PathVariable Long id) {
    Optional<Exercise> exercise = exerciseRepository.findById(id);
    return exercise.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // @PutMapping("/{id}")
  // public ResponseEntity<Exercise> updateExercise(@PathVariable Long id,
  // @RequestBody Exercise updatedExercise) {
  // return exerciseRepository.findById(id)
  // .map(exercise -> {
  // exercise.setName(updatedExercise.getName());
  // exercise.setVideoUrl(updatedExercise.getVideoUrl());
  // exercise.setPoint(updatedExercise.getPoint());
  // Exercise saved = exerciseRepository.save(exercise);
  // return ResponseEntity.ok(saved);
  // })
  // .orElse(ResponseEntity.notFound().build());
  // }

  // @DeleteMapping("/{id}")
  // public ResponseEntity<Void> deleteExercise(@PathVariable Long id) {
  // return exerciseRepository.findById(id)
  // .map(exercise -> {
  // exerciseRepository.delete(exercise);
  // return ResponseEntity.noContent().<Void>build();
  // })
  // .orElse(ResponseEntity.notFound().build());
  // }
}
