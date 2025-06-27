package exercise.Authentication.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoStepLoginRequestDTO {

  @NotNull
  private LoginRequestDTO loginDTO;

  private String code;
}
