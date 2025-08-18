package exercise.Exercise.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import exercise.Exercise.dtos.CreateExerciseDTO;
import exercise.Exercise.dtos.ExerciseDTO;
import exercise.Exercise.dtos.ExerciseProgressDTO;
import exercise.Exercise.dtos.ExerciseVideoProgressDTO;
import exercise.Exercise.dtos.NewVideoDTO;
import exercise.Exercise.dtos.UpdateExerciseDTO;
import exercise.Exercise.entities.Exercise;
import exercise.Exercise.entities.ExerciseVideo;
import exercise.Exercise.entities.ExerciseVideoProgress;
import exercise.Exercise.enums.ExercisePosition;
import exercise.Exercise.mappers.ExerciseMapper;
import exercise.Exercise.repositories.ExerciseRepository;
import exercise.Exercise.repositories.ExerciseVideoProgressRepository;
import exercise.Exercise.repositories.ExerciseVideoRepository;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;
import exercise.User.services.UserService;
import jakarta.transaction.Transactional;

@Service
public class ExerciseService {

  @Autowired
  private ExerciseRepository exerciseRepo;

  @Autowired
  private ExerciseVideoRepository exerciseVideoRepo;

  @Autowired
  private UserRepository userRepo;

  @Autowired
  private UserService userService;

  @Autowired
  private ExerciseVideoProgressRepository exerciseVideoProgressRepo;

  // @Autowired
  // private AchievementRepository achievementRepo;

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
    Exercise newExercise = new Exercise(null, exerciseDTO.getName(), exerciseDTO.getDescription(),
        exerciseDTO.getPoint(), null, admin, null, null);

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

  public ExerciseVideoProgressDTO progressExercise(Long exerciseId, Long videoId, BigDecimal progressDuration,
      User user) {
    LocalDateTime start = LocalDate.now().atStartOfDay();
    LocalDateTime end = start.plusDays(1);
    ExerciseVideoProgress videoProgress = exerciseVideoProgressRepo
        .findByUserIdAndVideoIdAndCreatedAtBetween(
            user.getId(),
            videoId,
            Timestamp.valueOf(start),
            Timestamp.valueOf(end))
        .orElse(new ExerciseVideoProgress(user, exerciseRepo.findById(exerciseId)
            .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId)),
            exerciseVideoRepo.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Exercise not found with video id: " + videoId))));

    videoProgress.setProgressDuration(progressDuration);
    ExerciseVideo video = exerciseVideoRepo.findById(videoId)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + videoId));
    BigDecimal EPS = BigDecimal.ONE;
    BigDecimal duration = BigDecimal.valueOf(video.getDurationSeconds());
    BigDecimal remaining = duration.subtract(progressDuration);

    if (remaining.compareTo(EPS) <= 0 && remaining.signum() >= 0) {
      videoProgress.setIsCompeleted(true);
    }

    ExerciseVideoProgress savedVideoProgress = exerciseVideoProgressRepo.save(videoProgress);
    return new ExerciseVideoProgressDTO(savedVideoProgress);
  }

  public List<ExerciseProgressDTO> getWeeklyActiveDaysProgress(Long userId, User actor) {
    if (Objects.isNull(actor) || userId != actor.getId() && !userService.checkUserConsentState(userId))
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "KVKK consent required");

    List<DayOfWeek> activeDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    LocalDate today = LocalDate.now();
    LocalDate monday = today.with(DayOfWeek.MONDAY);

    List<ExerciseProgressDTO> result = new ArrayList<>();

    for (DayOfWeek day : activeDays) {
      LocalDate targetDate = monday.with(day);
      LocalDateTime start = targetDate.atStartOfDay();
      LocalDateTime end = start.plusDays(1);

      List<ExerciseVideoProgress> videoProgress = exerciseVideoProgressRepo
          .findByUserIdAndCreatedAtBetween(
              userId,
              Timestamp.valueOf(start),
              Timestamp.valueOf(end));

      if (videoProgress.isEmpty())
        result.add(null);
      else {
        List<ExerciseVideoProgressDTO> videoProgressDTO = videoProgress.stream()
            .map(vp -> new ExerciseVideoProgressDTO(vp))
            .collect(Collectors.toList());
        BigDecimal totalProgress = videoProgress.stream()
            .map(p -> p != null && p.getProgressDuration() != null ? p.getProgressDuration() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.add(new ExerciseProgressDTO(userId,
            new ExerciseDTO(videoProgress.get(0).getExercise()), videoProgressDTO, totalProgress));
      }
    }

    return result;
  }

  public ExerciseProgressDTO getExerciseProgress(Long userId, User actor) {
    if (Objects.isNull(actor) || userId != actor.getId() && !userService.checkUserConsentState(userId))
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "KVKK consent required");
    ;

    LocalDateTime start = LocalDate.now().atStartOfDay();
    LocalDateTime end = start.plusDays(1);

    List<ExerciseVideoProgress> videoProgress = exerciseVideoProgressRepo
        .findByUserIdAndCreatedAtBetween(
            userId,
            Timestamp.valueOf(start),
            Timestamp.valueOf(end));

    if (videoProgress.isEmpty())
      return null;
    else {
      List<ExerciseVideoProgressDTO> videoProgressDTO = videoProgress.stream()
          .map(vp -> new ExerciseVideoProgressDTO(vp))
          .collect(Collectors.toList());
      BigDecimal totalProgress = videoProgress.stream()
          .map(p -> p != null && p.getProgressDuration() != null ? p.getProgressDuration() : BigDecimal.ZERO)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      return new ExerciseProgressDTO(userId,
          new ExerciseDTO(videoProgress.get(0).getExercise()), videoProgressDTO, totalProgress);
    }
  }

  public ExerciseProgressDTO getExerciseProgress(LocalDate date, Long userId) {
    LocalDateTime startOfDay = date.atStartOfDay(); // 00:00:00
    LocalDateTime endOfDay = startOfDay.plusDays(1); // ertesi gün 00:00:00

    List<ExerciseVideoProgress> videoProgress = exerciseVideoProgressRepo
        .findByUserIdAndCreatedAtBetween(
            userId,
            Timestamp.valueOf(
                startOfDay),
            Timestamp.valueOf(endOfDay));

    if (videoProgress.isEmpty())
      return null;
    else {
      List<ExerciseVideoProgressDTO> videoProgressDTO = videoProgress.stream()
          .map(vp -> new ExerciseVideoProgressDTO(vp))
          .collect(Collectors.toList());
      BigDecimal totalProgress = videoProgress.stream()
          .map(p -> p != null && p.getProgressDuration() != null ? p.getProgressDuration() : BigDecimal.ZERO)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      return new ExerciseProgressDTO(userId,
          new ExerciseDTO(videoProgress.get(0).getExercise()), videoProgressDTO, totalProgress);
    }
  }

  public void deleteExerciseProgress(Long exerciseId, LocalDate date, Long userId) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    List<ExerciseVideoProgress> videoProgress = exerciseVideoProgressRepo
        .findByUserIdAndCreatedAtBetween(
            userId,
            Timestamp.valueOf(
                startOfDay),
            Timestamp.valueOf(endOfDay));
    exerciseVideoProgressRepo.deleteAll(videoProgress);
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
