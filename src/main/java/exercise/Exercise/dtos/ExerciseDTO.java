package exercise.Exercise.dtos;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import exercise.Exercise.entities.Exercise;
import exercise.Exercise.entities.ExerciseVideo;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseDTO {

  private Long id;

  private String name;

  private String description;

  private Integer point;

  private List<ExerciseVideoDTO> videos;

  private Long adminId;

  private Timestamp createdAt;
  private Timestamp updatedAt;

  public ExerciseDTO(Exercise exercise) {
    this.id = exercise.getId();
    this.name = exercise.getName();
    this.description = exercise.getDescription();
    this.point = exercise.getPoint();
    this.videos = exercise.getVideos().stream().map(ExerciseVideoDTO::new).toList();
    this.adminId = exercise.getAdmin().getId();
    this.createdAt = exercise.getCreatedAt();
    this.updatedAt = exercise.getUpdatedAt();
  }
}