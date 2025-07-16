package exercise.Exercise.dtos;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

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

  private List<ExerciseVideo> videos;

  private Long admin_id;

  private Timestamp createdAt;
}