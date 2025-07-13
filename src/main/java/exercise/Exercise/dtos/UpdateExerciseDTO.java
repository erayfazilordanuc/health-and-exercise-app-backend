package exercise.Exercise.dtos;

import org.springframework.web.multipart.MultipartFile;

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
public class UpdateExerciseDTO {

  @NotNull
  private Long id;

  @NotNull
  private String name;

  private String description;

  private Integer point;

  @NotNull
  @NotEmpty
  private MultipartFile[] videoFiles;
}