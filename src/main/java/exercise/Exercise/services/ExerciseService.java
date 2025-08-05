package exercise.Exercise.services;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
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
import exercise.Exercise.dtos.NewVideoDTO;
import exercise.Exercise.dtos.UpdateExerciseDTO;
import exercise.Exercise.entities.Achievement;
import exercise.Exercise.entities.Exercise;
import exercise.Exercise.entities.ExerciseProgress;
import exercise.Exercise.entities.ExerciseVideo;
import exercise.Exercise.enums.ExercisePosition;
import exercise.Exercise.mappers.ExerciseMapper;
import exercise.Exercise.repositories.AchievementRepository;
import exercise.Exercise.repositories.ExerciseProgressRepository;
import exercise.Exercise.repositories.ExerciseRepository;
import exercise.Exercise.repositories.ExerciseVideoRepository;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;
import jakarta.transaction.Transactional;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;

@Service
public class ExerciseService {

  @Autowired
  private ExerciseRepository exerciseRepo;

  @Autowired
  private ExerciseVideoRepository exerciseVideoRepo;

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

  public ExerciseDTO getTodayExerciseByPosition(ExercisePosition position) {
    // TO DO (Imp) Burada ilerleyen zamanlarda güne özel egzersizler çekilebilir

    Long seatedId = (long) 48;
    Long standingId = (long) 47;
    return switch (position) {
      case SEATED -> exerciseRepo.findById(seatedId).map(exerciseMapper::entityToDto).get();
      case STANDING -> exerciseRepo.findById(standingId).map(exerciseMapper::entityToDto).get();
    };
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
    }
    newExerciseProgress.setProgressRatio(progressRatio);

    ExerciseProgress savedExerciseProgress = exerciseProgressRepo.save(newExerciseProgress);
    ExerciseProgressDTO newExerciseProgressDTO = new ExerciseProgressDTO(savedExerciseProgress);
    return newExerciseProgressDTO;
  }

  public List<ExerciseProgressDTO> getWeeklyActiveDaysProgress(User user) {
    List<DayOfWeek> activeDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    LocalDate today = LocalDate.now();
    LocalDate monday = today.with(DayOfWeek.MONDAY);

    List<ExerciseProgressDTO> result = new ArrayList<>();

    for (DayOfWeek day : activeDays) {
      LocalDate targetDate = monday.with(day);
      LocalDateTime start = targetDate.atStartOfDay();
      LocalDateTime end = start.plusDays(1);

      ExerciseProgress progress = exerciseProgressRepo
          .findByUserIdAndCreatedAtBetween(
              user.getId(),
              Timestamp.valueOf(start),
              Timestamp.valueOf(end));

      result.add(progress != null ? new ExerciseProgressDTO(progress) : null);
    }

    return result;
  }

  public List<ExerciseProgressDTO> getWeeklyActiveDaysProgress(Long userId) {
    List<DayOfWeek> activeDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    LocalDate today = LocalDate.now();
    LocalDate monday = today.with(DayOfWeek.MONDAY);

    List<ExerciseProgressDTO> result = new ArrayList<>();

    for (DayOfWeek day : activeDays) {
      LocalDate targetDate = monday.with(day);
      LocalDateTime start = targetDate.atStartOfDay();
      LocalDateTime end = start.plusDays(1);

      ExerciseProgress progress = exerciseProgressRepo
          .findByUserIdAndCreatedAtBetween(
              userId,
              Timestamp.valueOf(start),
              Timestamp.valueOf(end));

      result.add(progress != null ? new ExerciseProgressDTO(progress) : null);
    }

    return result;
  }

  public ExerciseProgressDTO getExerciseProgress(User user) {
    LocalDateTime start = LocalDate.now().atStartOfDay();
    LocalDateTime end = start.plusDays(1);
    ExerciseProgress progress = exerciseProgressRepo.findByUserIdAndCreatedAtBetween(
        user.getId(),
        Timestamp.valueOf(start),
        Timestamp.valueOf(end));

    if (progress == null) {
      return null;
    }

    return new ExerciseProgressDTO(progress);
  }

  public ExerciseProgressDTO getExerciseProgress(Long userId) {
    LocalDateTime start = LocalDate.now().atStartOfDay();
    LocalDateTime end = start.plusDays(1);
    ExerciseProgress progress = exerciseProgressRepo.findByUserIdAndCreatedAtBetween(
        userId,
        Timestamp.valueOf(start),
        Timestamp.valueOf(end));

    if (progress == null) {
      return null;
    }

    return new ExerciseProgressDTO(progress);
  }

  public ExerciseProgressDTO getExerciseProgress(LocalDate date, User user) {
    LocalDateTime startOfDay = date.atStartOfDay(); // 00:00:00
    LocalDateTime endOfDay = startOfDay.plusDays(1); // ertesi gün 00:00:00

    ExerciseProgress progress = exerciseProgressRepo
        .findByUserIdAndCreatedAtBetween(
            user.getId(),
            Timestamp.valueOf(startOfDay),
            Timestamp.valueOf(endOfDay));

    if (progress == null) {
      return null;
    }

    return new ExerciseProgressDTO(progress);
  }

  public void deleteExerciseProgress(Long exerciseId, LocalDate date, User user) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    ExerciseProgress existExerciseProgress = exerciseProgressRepo
        .findByUserIdAndExerciseIdAndCreatedAtBetween(user.getId(), exerciseId, Timestamp.valueOf(
            startOfDay),
            Timestamp.valueOf(
                endOfDay));
    exerciseProgressRepo.delete(existExerciseProgress);
  }

  // public static double getDurationSec(String url) throws IOException {
  // try (ReadableByteChannel ch = Channels.newChannel(new URL(url).openStream());
  // IsoFile iso = new IsoFile(ch)) {

  // MovieHeaderBox mvhd = iso
  // .getBoxes(MovieHeaderBox.class, true)
  // .get(0); // moov → mvhd

  // return (double) mvhd.getDuration() / mvhd.getTimescale();
  // }
  // }

  public ExerciseDTO addVideo(Long exerciseId, NewVideoDTO videoDTO, User user) throws IOException {
    Exercise existExercise = exerciseRepo.findById(exerciseId)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

    if (!existExercise.getAdmin().getId().equals(user.getId()))
      throw new RuntimeException("You can not add video to an exercise for someone else");

    String cleanedUrl = videoDTO.getVideoUrl().replaceAll("^\"|\"$", "");

    ExerciseVideo newVideo = new ExerciseVideo(null, videoDTO.getName(), cleanedUrl,
        videoDTO.getDurationSeconds(), existExercise,
        null);

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

  // exercise id kaldırılabilir
  public ExerciseDTO updateVideo(Long videoId, Long exerciseId, NewVideoDTO videoDTO, User user) throws IOException {
    Exercise existExercise = exerciseRepo.findById(exerciseId)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

    if (!existExercise.getAdmin().getId().equals(user.getId()))
      throw new RuntimeException("You can not add video to an exercise for someone else");

    ExerciseVideo video = exerciseVideoRepo.findById(videoId)
        .orElseThrow(() -> new RuntimeException("Video not found with id: " + videoId));

    video.setName(videoDTO.getName());
    // video.setVideoUrl(videoDTO.getVideoUrl());
    // video.setDurationSeconds(videoDTO.getDurationSeconds());

    exerciseVideoRepo.save(video);

    Exercise savedExercise = exerciseRepo.findById(exerciseId).get();

    ExerciseDTO savedExerciseDTO = exerciseMapper.entityToDto(savedExercise);

    return savedExerciseDTO;
  }

  public String getPresignedUrl(Long exerciseId, String fileName, String folder) {
    return s3Service.generatePresignedUploadUrl(exerciseId, fileName, folder, Duration.ofMinutes(60));
  }

  public Exercise deleteVideo(Long id, Long exerciseId, User user) throws IOException {
    Exercise exercise = exerciseRepo.findById(exerciseId)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

    if (!exercise.getAdmin().getId().equals(user.getId()))
      throw new RuntimeException("You can not delete video from an exercise for someone else");

    ExerciseVideo video = exerciseVideoRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("Video not found with id: " + id));

    exerciseVideoRepo.delete(video);

    Exercise updatedExercise = exerciseRepo.findById(exerciseId)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

    return updatedExercise;
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
