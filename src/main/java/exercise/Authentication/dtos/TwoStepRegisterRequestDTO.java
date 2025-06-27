package exercise.Authentication.dtos;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoStepRegisterRequestDTO {

  @NotNull
  private RegisterRequestDTO registerDTO;

  @Pattern(regexp = "\\d{6}")
  private String code;
}
