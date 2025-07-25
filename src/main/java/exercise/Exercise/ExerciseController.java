package exercise.Exercise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import exercise.Exercise.dtos.CreateExerciseDTO;
import exercise.Exercise.dtos.ExerciseDTO;
import exercise.Exercise.dtos.ExerciseProgressDTO;
import exercise.Exercise.dtos.NewVideoDTO;
import exercise.Exercise.dtos.UpdateExerciseDTO;
import exercise.Exercise.services.ExerciseService;
import exercise.User.entities.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@Tags(value = @Tag(name = "Exercise Operations"))
public class ExerciseController {

  @Autowired
  private ExerciseService exerciseService;

  // @Autowired
  // private UserService userService;

  @GetMapping
  public List<ExerciseDTO> getAllExercises() {
    return exerciseService.getAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ExerciseDTO> getExerciseById(@PathVariable Long id) {
    ExerciseDTO exercise = exerciseService.getById(id);
    return ResponseEntity.ok(exercise);
  }

  @PutMapping("/{exerciseId}/progress/{progressRatio}")
  public ExerciseProgressDTO progressExercise(@PathVariable Long exerciseId, @PathVariable Integer progressRatio,
      @AuthenticationPrincipal User user) {
    ExerciseProgressDTO exerciseProgress = exerciseService.progressExercise(exerciseId, progressRatio, user);
    return exerciseProgress;
  }

  @GetMapping("/{exerciseId}/daily/progress")
  public ExerciseProgressDTO getTodaysTotalExerciseProgress(@PathVariable Long exerciseId,
      @AuthenticationPrincipal User user) {
    ExerciseProgressDTO exerciseProgress = exerciseService.getExerciseProgress(exerciseId, user);
    return exerciseProgress;
  }

  @GetMapping("/{exerciseId}/date/{date}/progress")
  public ExerciseProgressDTO getExerciseProgressByExerciseId(@PathVariable Long exerciseId,
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal User user) {
    ExerciseProgressDTO exerciseProgress = exerciseService.getExerciseProgress(exerciseId, date, user);
    return exerciseProgress;
  }

  // @PostMapping("/{id}/achievement")
  // public List<AchievementDTO> completeExercise(@PathVariable Long id,
  // @AuthenticationPrincipal User user) {
  // List<AchievementDTO> achievements = exerciseService.completeExercise(id,
  // user.getId());
  // return achievements;
  // }

  // @GetMapping("/{id}/achievements")
  // public List<AchievementDTO> getAchievementByUserId(@PathVariable Long id) {
  // return userService.getAchievements(id);
  // }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ExerciseDTO createExercise(@RequestBody CreateExerciseDTO exerciseDTO,
      @AuthenticationPrincipal User user) throws IOException {
    ExerciseDTO savedExerciseDTO = exerciseService.create(exerciseDTO, user);
    return savedExerciseDTO;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<ExerciseDTO> updateExercise(@PathVariable Long id,
      @RequestBody UpdateExerciseDTO updateExerciseDTO,
      @AuthenticationPrincipal User user) {
    ExerciseDTO savedExerciseDTO = exerciseService.update(id, updateExerciseDTO, user);
    return ResponseEntity.ok(savedExerciseDTO);
  }

  @PreAuthorize("hasRole('ADMIN')")
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

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{exerciseId}/videos")
  public ResponseEntity<ExerciseDTO> addVideoToExercise(@PathVariable Long exerciseId,
      @RequestBody NewVideoDTO newVideoDTO,
      @AuthenticationPrincipal User user) {
    String cleanedUrl = newVideoDTO.getVideoUrl().replaceAll("^\"|\"$", "");
    ExerciseDTO exerciseDTO = exerciseService.addVideo(exerciseId, cleanedUrl, user);
    return ResponseEntity.ok(exerciseDTO);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{exerciseId}/videos")
  public ResponseEntity<Void> deleteVideoFromExercise(@PathVariable Long exerciseId,
      @RequestParam String videoUrl,
      @AuthenticationPrincipal User user) throws IOException {
    exerciseService.deleteVideo(exerciseId, videoUrl, user);
    return ResponseEntity.noContent().build();
  }
}
