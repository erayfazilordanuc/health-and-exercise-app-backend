package exercise.Exercise.dtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseProgressDTO {
  private Long userId;

  private ExerciseDTO exerciseDTO;

  private List<ExerciseVideoProgressDTO> videoProgress;

  private BigDecimal totalProgressDuration;
}
