package exercise.Exercise.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewVideoDTO {

  @NotNull
  private String videoUrl;

  @NotNull
  private String name;

  @NotNull
  private Integer durationSeconds;
}
