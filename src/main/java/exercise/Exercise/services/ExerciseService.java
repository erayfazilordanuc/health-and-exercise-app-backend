package exercise.Exercise.services;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import exercise.Exercise.dtos.CreateExerciseDTO;
import exercise.Exercise.dtos.ExerciseDTO;
import exercise.Exercise.dtos.UpdateExerciseDTO;
import exercise.Exercise.entities.Exercise;
import exercise.Exercise.entities.ExerciseVideo;
import exercise.Exercise.mappers.ExerciseMapper;
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
  private S3Service s3Service;

  @Autowired
  private ExerciseMapper exerciseMapper;

  // TO DO burada exerciseDTO içindeki exerciseVideos artık exerciseVideoDTO olarak dönsün
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
      throw new RuntimeException("You can not delete exercise for someone else");

    existExercise.setName(updatedExerciseDTO.getName());
    existExercise.setDescription(updatedExerciseDTO.getDescription());
    existExercise.setPoint(updatedExerciseDTO.getPoint());

    Exercise savedExercise = exerciseRepo.save(existExercise);

    ExerciseDTO savedExerciseDTO = exerciseMapper.entityToDto(savedExercise);

    return savedExerciseDTO;
  }

  public ExerciseDTO addVideo(Long exerciseId, String videoUrl, User user) {
    Exercise existExercise = exerciseRepo.findById(exerciseId)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

    if (!existExercise.getAdmin().getId().equals(user.getId()))
      throw new RuntimeException("You can not delete exercise for someone else");

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
    return s3Service.generatePresignedUploadUrl(exerciseId, fileName, folder, Duration.ofMinutes(15));
  }

  public Exercise deleteVideo(Long exerciseId, String videoUrl) throws IOException {
    Exercise exercise = exerciseRepo.findById(exerciseId)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

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
