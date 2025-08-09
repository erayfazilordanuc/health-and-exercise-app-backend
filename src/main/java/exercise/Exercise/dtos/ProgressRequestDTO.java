package exercise.Exercise.dtos;

import java.math.BigDecimal;

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
public class ProgressRequestDTO {

  @NotNull
  private BigDecimal seconds;
}