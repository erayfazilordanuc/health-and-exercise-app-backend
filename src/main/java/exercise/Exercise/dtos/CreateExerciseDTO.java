package exercise.Exercise.dtos;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateExerciseDTO {

  @NotNull
  private String name;

  @NotNull
  private MultipartFile videoFile;

  @NotNull
  private Integer point;
}