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
import exercise.Exercise.dtos.ExerciseVideoProgressDTO;
import exercise.Exercise.dtos.NewVideoDTO;
import exercise.Exercise.dtos.ProgressRequestDTO;
import exercise.Exercise.dtos.UpdateExerciseDTO;
import exercise.Exercise.enums.ExercisePosition;
import exercise.Exercise.services.ExerciseService;
import exercise.User.entities.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import java.io.IOException;
import java.math.BigDecimal;
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

  @GetMapping("/today")
  public ResponseEntity<ExerciseDTO> findTodayExercise(
      @RequestParam ExercisePosition position,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(exerciseService.getTodayExerciseByPosition(position));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ExerciseDTO> getExerciseById(@PathVariable Long id) {
    ExerciseDTO exercise = exerciseService.getById(id);
    return ResponseEntity.ok(exercise);
  }

  @PutMapping("/{exerciseId}/video/{videoId}/progress")
  public ExerciseVideoProgressDTO progressExercise(@PathVariable Long exerciseId, @PathVariable Long videoId,
      @RequestBody ProgressRequestDTO progressDTO,
      @AuthenticationPrincipal User user) {
    ExerciseVideoProgressDTO exerciseProgress = exerciseService.progressExercise(exerciseId, videoId,
        progressDTO.getSeconds(),
        user);
    return exerciseProgress;
  }

  @GetMapping("/weekly-active-days/progress")
  public List<ExerciseProgressDTO> getWeeklyActiveDaysProgress(@AuthenticationPrincipal User user) {
    List<ExerciseProgressDTO> exerciseProgress = exerciseService.getWeeklyActiveDaysProgress(user.getId(), null);
    return exerciseProgress;
  }

  @GetMapping("/daily/progress")
  public ExerciseProgressDTO getTodaysExerciseProgress(
      @AuthenticationPrincipal User user) {
    ExerciseProgressDTO exerciseProgress = exerciseService.getExerciseProgress(user.getId(), user);
    return exerciseProgress;
  }

  @GetMapping("/date/{date}/progress")
  public ExerciseProgressDTO getExerciseProgressByDate(
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal User user) {
    ExerciseProgressDTO exerciseProgress = exerciseService.getExerciseProgress(date, user.getId());
    return exerciseProgress;
  }

  @DeleteMapping("/{exerciseId}/date/{date}/progress")
  public void deleteExerciseProgress(@PathVariable Long exerciseId,
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal User user) {
    exerciseService.deleteExerciseProgress(exerciseId, date, user.getId());
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
  @GetMapping("/weekly-active-days/progress/{userId}")
  public List<ExerciseProgressDTO> getWeeklyActiveDaysProgressByUserId(@PathVariable Long userId,
      @AuthenticationPrincipal User user) {
    List<ExerciseProgressDTO> exerciseProgress = exerciseService.getWeeklyActiveDaysProgress(userId, user);
    return exerciseProgress;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/daily/progress/{userId}")
  public ExerciseProgressDTO getTodaysExerciseProgressByUserId(
      @PathVariable Long userId, @AuthenticationPrincipal User user) {
    ExerciseProgressDTO exerciseProgress = exerciseService.getExerciseProgress(userId, user);
    return exerciseProgress;
  }

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
      @AuthenticationPrincipal User user) throws IOException {
    ExerciseDTO exerciseDTO = exerciseService.addVideo(exerciseId, newVideoDTO, user);
    return ResponseEntity.ok(exerciseDTO);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{exerciseId}/videos/id/{id}")
  public ResponseEntity<ExerciseDTO> updateExerciseVideo(@PathVariable Long exerciseId, @PathVariable Long id,
      @RequestBody NewVideoDTO newVideoDTO,
      @AuthenticationPrincipal User user) throws IOException {
    ExerciseDTO exerciseDTO = exerciseService.updateVideo(id, exerciseId, newVideoDTO, user);
    return ResponseEntity.ok(exerciseDTO);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{exerciseId}/videos/id/{id}")
  public ResponseEntity<Void> deleteVideoFromExercise(@PathVariable Long exerciseId, @PathVariable Long id,
      @AuthenticationPrincipal User user) throws IOException {
    exerciseService.deleteVideo(id, exerciseId, user);
    return ResponseEntity.noContent().build();
  }
}
