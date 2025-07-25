package exercise.Exercise.services;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import exercise.Exercise.dtos.AchievementDTO;
import exercise.Exercise.dtos.CreateExerciseDTO;
import exercise.Exercise.dtos.ExerciseDTO;
import exercise.Exercise.dtos.ExerciseProgressDTO;
import exercise.Exercise.dtos.UpdateExerciseDTO;
import exercise.Exercise.entities.Achievement;
import exercise.Exercise.entities.Exercise;
import exercise.Exercise.entities.ExerciseProgress;
import exercise.Exercise.entities.ExerciseVideo;
import exercise.Exercise.mappers.ExerciseMapper;
import exercise.Exercise.repositories.AchievementRepository;
import exercise.Exercise.repositories.ExerciseProgressRepository;
import exercise.Exercise.repositories.ExerciseRepository;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class ExerciseService {

  @Autowired
  private ExerciseRepository exerciseRepo;

  @Autowired
  private UserRepository userRepo;

  @Autowired
  private ExerciseProgressRepository exerciseProgressRepo;

  @Autowired
  private AchievementRepository achievementRepo;

  @Autowired
  private S3Service s3Service;

  @Autowired
  private ExerciseMapper exerciseMapper;

  // TO DO burada exerciseDTO içindeki exerciseVideos artık exerciseVideoDTO
  // olarak dönsün
  public List<ExerciseDTO> getAll() {
    return exerciseRepo.findAll().stream()
        .map(exerciseMapper::entityToDto)
        .toList();
  }

  public ExerciseDTO getById(Long id) {
    return exerciseRepo.findById(id).map(exerciseMapper::entityToDto)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));
  }

  @Transactional
  public ExerciseDTO create(CreateExerciseDTO exerciseDTO, User admin) throws IOException {
    User adminWithAchievements = userRepo.findById(admin.getId()).get();
    Exercise newExercise = new Exercise(null, exerciseDTO.getName(), exerciseDTO.getDescription(),
        exerciseDTO.getPoint(), null, adminWithAchievements, null, null);

    Exercise savedExercise = exerciseRepo.save(newExercise);

    ExerciseDTO savedExerciseDTO = exerciseMapper.entityToDto(savedExercise);

    return savedExerciseDTO;
  }

  public ExerciseDTO update(Long exerciseId, UpdateExerciseDTO updatedExerciseDTO, User user) {
    Exercise existExercise = exerciseRepo.findById(exerciseId)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

    if (!existExercise.getAdmin().getId().equals(user.getId()))
      throw new RuntimeException("You can not update exercise for someone else");

    existExercise.setName(updatedExerciseDTO.getName());
    existExercise.setDescription(updatedExerciseDTO.getDescription());
    existExercise.setPoint(updatedExerciseDTO.getPoint());

    Exercise savedExercise = exerciseRepo.save(existExercise);

    ExerciseDTO savedExerciseDTO = exerciseMapper.entityToDto(savedExercise);

    return savedExerciseDTO;
  }

  public ExerciseProgressDTO progressExercise(Long exerciseId, Integer progressRatio, User user) {
    ExerciseProgress newExerciseProgress = new ExerciseProgress();

    LocalDateTime start = LocalDate.now().atStartOfDay();
    LocalDateTime end = start.plusDays(1);
    ExerciseProgress existExerciseProgress = exerciseProgressRepo.findByUserIdAndExerciseIdAndCreatedAtBetween(
        user.getId(),
        exerciseId,
        Timestamp.valueOf(start),
        Timestamp.valueOf(end));

    if (existExerciseProgress != null) {
      newExerciseProgress = existExerciseProgress;
    } else {
      newExerciseProgress.setUser(user);
      Exercise exercise = exerciseRepo.findById(exerciseId)
          .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));
      newExerciseProgress.setExercise(exercise);
      newExerciseProgress.setProgressRatio(progressRatio);
    }

    ExerciseProgress savedExerciseProgress = exerciseProgressRepo.save(newExerciseProgress);
    ExerciseProgressDTO newExerciseProgressDTO = new ExerciseProgressDTO(savedExerciseProgress);
    return newExerciseProgressDTO;
  }

  public List<ExerciseProgressDTO> getWeeklyActiveDaysExerciseProgress(Long exerciseId, User user) {
    List<DayOfWeek> activeDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    LocalDate today = LocalDate.now();
    LocalDate monday = today.with(DayOfWeek.MONDAY);

    List<ExerciseProgressDTO> result = new ArrayList<>();

    for (DayOfWeek day : activeDays) {
      LocalDate targetDate = monday.with(day);
      LocalDateTime start = targetDate.atStartOfDay();
      LocalDateTime end = start.plusDays(1);

      ExerciseProgress progress = exerciseProgressRepo
          .findByUserIdAndExerciseIdAndCreatedAtBetween(
              user.getId(),
              exerciseId,
              Timestamp.valueOf(start),
              Timestamp.valueOf(end));

      result.add(progress != null ? new ExerciseProgressDTO(progress) : null);
    }

    return result;
  }

  public ExerciseProgressDTO getExerciseProgress(Long exerciseId, User user) {
    LocalDateTime start = LocalDate.now().atStartOfDay();
    LocalDateTime end = start.plusDays(1);
    return new ExerciseProgressDTO(
        exerciseProgressRepo.findByUserIdAndExerciseIdAndCreatedAtBetween(
            user.getId(),
            exerciseId,
            Timestamp.valueOf(start),
            Timestamp.valueOf(end)));
  }

  public ExerciseProgressDTO getExerciseProgress(Long exerciseId, LocalDate date, User user) {
    LocalDateTime startOfDay = date.atStartOfDay(); // 00:00:00
    LocalDateTime endOfDay = startOfDay.plusDays(1); // ertesi gün 00:00:00

    ExerciseProgress progress = exerciseProgressRepo
        .findByUserIdAndExerciseIdAndCreatedAtBetween(
            user.getId(),
            exerciseId,
            Timestamp.valueOf(startOfDay),
            Timestamp.valueOf(endOfDay));

    if (progress == null) {
      throw new RuntimeException("Belirtilen tarihte kayıt bulunamadı.");
    }

    return new ExerciseProgressDTO(progress);
  }

  public List<AchievementDTO> completeExercise(Long id, Long userId) {
    Exercise exercise = exerciseRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + id));

    achievementRepo.findByUserIdAndExerciseId(userId, id)
        .ifPresent(a -> {
          throw new RuntimeException("Achievement already exists for user " + userId + " and exercise " + id);
        });

    User userEntity = userRepo.findById(userId).get();

    Achievement newAchievement = new Achievement(null, userEntity, exercise, null);

    List<Achievement> existAchievements = userEntity.getAchievements();
    existAchievements.add(newAchievement);

    userEntity.setAchievements(existAchievements);
    userRepo.save(userEntity);

    return userEntity.getAchievements().stream().map(AchievementDTO::new).toList();
  }

  public ExerciseDTO addVideo(Long exerciseId, String videoUrl, User user) {
    Exercise existExercise = exerciseRepo.findById(exerciseId)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

    if (!existExercise.getAdmin().getId().equals(user.getId()))
      throw new RuntimeException("You can not add video to an exercise for someone else");

    ExerciseVideo newVideo = new ExerciseVideo(null, videoUrl, existExercise, null);

    List<ExerciseVideo> exerciseVideos = existExercise.getVideos();

    List<String> videoUrls = exerciseVideos.stream().map(ExerciseVideo::getVideoUrl)
        .collect(Collectors.toList());

    if (videoUrls.contains(newVideo.getVideoUrl())) {
      throw new RuntimeException("Video is already exist in exercise");
    }

    exerciseVideos.add(newVideo);

    existExercise.setVideos(exerciseVideos);

    Exercise savedExercise = exerciseRepo.save(existExercise);

    ExerciseDTO savedExerciseDTO = exerciseMapper.entityToDto(savedExercise);

    return savedExerciseDTO;
  }

  public String getPresignedUrl(Long exerciseId, String fileName, String folder) {
    return s3Service.generatePresignedUploadUrl(exerciseId, fileName, folder, Duration.ofMinutes(60));
  }

  public Exercise deleteVideo(Long exerciseId, String videoUrl, User user) throws IOException {
    Exercise exercise = exerciseRepo.findById(exerciseId)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

    if (!exercise.getAdmin().getId().equals(user.getId()))
      throw new RuntimeException("You can not delete video from an exercise for someone else");

    Set<String> existingFileNames = exercise.getVideos().stream()
        .map(video -> {
          String url = video.getVideoUrl();
          return url.substring(url.lastIndexOf('/') + 1);
        })
        .collect(Collectors.toSet());

    if (existingFileNames.contains(videoUrl)) {
      s3Service.deleteObject(videoUrl);
    }

    List<ExerciseVideo> updatedVideos = exercise.getVideos().stream()
        .filter(video -> {
          return !video.getVideoUrl().equals(videoUrl);
        })
        .collect(Collectors.toList());

    exercise.setVideos(updatedVideos);

    return exerciseRepo.save(exercise);
  }

  public void delete(Long id, User user) throws IOException {
    Exercise exercise = exerciseRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + id));

    if (!exercise.getAdmin().getId().equals(user.getId()))
      throw new RuntimeException("You can not delete exercise for someone else");

    exercise.getVideos().stream()
        .forEach(video -> {
          try {
            s3Service.deleteObject(video.getVideoUrl());
          } catch (IOException e) {
            e.printStackTrace();
          }
        });

    exerciseRepo.delete(exercise);
  }
}
