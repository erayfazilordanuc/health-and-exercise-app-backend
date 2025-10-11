package exercise.Authentication.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDTO {

    @NotEmpty
    private String email;

    @NotEmpty
    private String code;
}
