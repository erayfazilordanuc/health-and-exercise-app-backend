package exercise.Exercise.services;

import java.io.IOException;
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
import exercise.Exercise.entities.Exercise;
import exercise.Exercise.entities.ExerciseVideo;
import exercise.Exercise.repositories.ExerciseRepository;

@Service
public class ExerciseService {

  @Autowired
  private ExerciseRepository exerciseRepo;

  @Autowired
  private S3Service s3Service;

  public Exercise create(CreateExerciseDTO exerciseDTO) throws IOException {
    Exercise newExercise = new Exercise(null, exerciseDTO.getName(), exerciseDTO.getDescription(),
        exerciseDTO.getPoint(), null, null, null);
    Exercise savedExercise = exerciseRepo.save(newExercise);

    List<ExerciseVideo> videos = Arrays.stream(exerciseDTO.getVideoFiles())
        .map(file -> {
          try {
            // TO DO burada videos a kaydetmesin herşeyi
            String fileUrl = s3Service.uploadObject(savedExercise.getId(), file, "videos");
            return new ExerciseVideo(null, fileUrl, savedExercise);
          } catch (IOException e) {
            e.printStackTrace();
            return null;
          }
        })
        .collect(Collectors.toList());
    savedExercise.setVideos(videos);

    Exercise savedExerciseWithVideo = exerciseRepo.save(savedExercise);

    return savedExerciseWithVideo;
  }

  public Exercise update(Exercise updatedExercise) {
    Exercise savedExercise = exerciseRepo.save(updatedExercise);
    return savedExercise;
  }

  public Exercise uploadObjectToExercise(Long exerciseId, MultipartFile[] objectFiles) throws IOException {
    Exercise exercise = exerciseRepo.findById(exerciseId)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

    Set<String> existingFileNames = exercise.getVideos().stream()
        .map(video -> {
          String url = video.getVideoUrl();
          return url.substring(url.lastIndexOf('/') + 1);
        })
        .collect(Collectors.toSet());

    for (MultipartFile file : objectFiles) {
      String originalFilename = file.getOriginalFilename();
      if (originalFilename != null && existingFileNames.contains(originalFilename)) {
        System.out.println("File already exists: " + originalFilename);
        continue;
      }

      String fileUrl = s3Service.uploadObject(exerciseId, file, "videos");

      ExerciseVideo newVideo = new ExerciseVideo(null, fileUrl, exercise);

      exercise.getVideos().add(newVideo);
    }

    return exerciseRepo.save(exercise);
  }

  public Exercise deleteObjectFromExercise(Long exerciseId, String objectUrl) throws IOException {
    Exercise exercise = exerciseRepo.findById(exerciseId)
        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

    Set<String> existingFileNames = exercise.getVideos().stream()
        .map(video -> {
          String url = video.getVideoUrl();
          return url.substring(url.lastIndexOf('/') + 1);
        })
        .collect(Collectors.toSet());

    if (existingFileNames.contains(objectUrl)) {
      s3Service.deleteObject(objectUrl);
    }

    List<ExerciseVideo> updatedVideos = exercise.getVideos().stream()
        .filter(video -> {
          return !video.getVideoUrl().equals(objectUrl);
        })
        .collect(Collectors.toList());

    exercise.setVideos(updatedVideos);

    return exerciseRepo.save(exercise);
  }

  public List<Exercise> getAll() {
    List<Exercise> exercises = exerciseRepo.findAll();
    return exercises;
  }

  public Exercise getById(Long id) {
    return exerciseRepo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));
  }

  public void delete(Long id) throws IOException {
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

  public void deleteObject(String objectUrl) throws IOException {
    s3Service.deleteObject(objectUrl);
  }
}
