package exercise.Exercise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import exercise.Exercise.dtos.CreateExerciseDTO;
import exercise.Exercise.dtos.ExerciseDTO;
import exercise.Exercise.dtos.UpdateExerciseDTO;
import exercise.Exercise.entities.Achievement;
import exercise.Exercise.entities.Exercise;
import exercise.Exercise.entities.ExerciseVideo;
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

  @GetMapping
  public List<ExerciseDTO> getAllExercises() {
    return exerciseService.getAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ExerciseDTO> getExerciseById(@PathVariable Long id) {
    ExerciseDTO exercise = exerciseService.getById(id);
    return ResponseEntity.ok(exercise);
  }

  @PostMapping("/{id}/achievement")
  public Achievement completeExercise(@PathVariable Long id, @AuthenticationPrincipal User user) {
    return null;
  }

  // TO DO add preAuthorization
  // @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ExerciseDTO createExercise(@RequestBody CreateExerciseDTO exerciseDTO,
      @AuthenticationPrincipal User user) throws IOException {
    ExerciseDTO savedExerciseDTO = exerciseService.create(exerciseDTO, user);
    return savedExerciseDTO;
  }

  // @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<ExerciseDTO> updateExercise(@PathVariable Long id,
      @RequestBody UpdateExerciseDTO updateExerciseDTO,
      @AuthenticationPrincipal User user) {
    ExerciseDTO savedExerciseDTO = exerciseService.update(id, updateExerciseDTO, user);
    return ResponseEntity.ok(savedExerciseDTO);
  }

  // @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteExercise(@PathVariable Long id,
      @AuthenticationPrincipal User user) throws IOException {
    exerciseService.delete(id, user);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/videos/presign")
  public String presignVideo(@PathVariable("id") Long exerciseId,
      @RequestParam String fileName,
      @AuthenticationPrincipal User user) {
    return exerciseService.getPresignedUrl(exerciseId, fileName, "videos");
  }

  // @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{exerciseId}/videos")
  public ResponseEntity<ExerciseDTO> addVideoToExercise(@PathVariable Long exerciseId,
      @RequestBody String videoUrl,
      @AuthenticationPrincipal User user) {
    ExerciseDTO exerciseDTO = exerciseService.addVideo(exerciseId, videoUrl, user);
    return ResponseEntity.ok(exerciseDTO);
  }

  // @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}/videos/url/{videoUrl}")
  public ResponseEntity<Void> deleteVideoFromExercise(@PathVariable Long exerciseId,
      @PathVariable String videoUrl,
      @AuthenticationPrincipal User user) throws IOException {
    exerciseService.deleteVideo(exerciseId, videoUrl);
    return ResponseEntity.noContent().build();
  }
}
