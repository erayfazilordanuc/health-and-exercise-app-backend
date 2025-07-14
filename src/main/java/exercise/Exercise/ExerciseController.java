package exercise.Exercise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import exercise.Exercise.dtos.CreateExerciseDTO;
import exercise.Exercise.entities.Achievement;
import exercise.Exercise.entities.Exercise;
import exercise.Exercise.services.ExerciseService;
import exercise.User.entities.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@Tags(value = @Tag(name = "Exercise Operations"))
public class ExerciseController {

  @Autowired
  private ExerciseService exerciseService;

  // TO DO add preAuthorization
  // @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Exercise createExercise(@ModelAttribute CreateExerciseDTO exerciseDTO) throws IOException {
    Exercise exercise = exerciseService.create(exerciseDTO);
    return exercise;
  }

  @PostMapping("/{id}/achievement")
  public Achievement completeExercise(@PathVariable Long id, @AuthenticationPrincipal User user) {
    return null;
  }

  @GetMapping
  public List<Exercise> getAllExercises() {
    return exerciseService.getAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Exercise> getExerciseById(@PathVariable Long id) {
    Exercise exercise = exerciseService.getById(id);
    return ResponseEntity.ok(exercise);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Exercise> updateExercise(@RequestBody Exercise updatedExercise) {
    Exercise exercise = exerciseService.update(updatedExercise);
    return ResponseEntity.ok(exercise);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteExercise(@PathVariable Long id) throws IOException {
    exerciseService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/objects")
  public ResponseEntity<Exercise> uploadObjectToExercise(@RequestBody Exercise updatedExercise) {
    Exercise exercise = exerciseService.update(updatedExercise);
    return ResponseEntity.ok(exercise);
  }

  @DeleteMapping("/{id}/objects/{objectUrl}")
  public ResponseEntity<Void> deleteObjectFromExercise(@PathVariable Long id,
      @PathVariable String objectUrl) throws IOException {
    exerciseService.delete(id);
    return ResponseEntity.noContent().build();
  }

  // @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/object")
  public void deleteObject(@PathVariable String objectUrl) throws IOException {
    exerciseService.deleteObject(objectUrl);
  }
}
