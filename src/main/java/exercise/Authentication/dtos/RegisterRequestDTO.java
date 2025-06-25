package exercise.Authentication.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {

    @NotEmpty
    private String username;

    // @NotEmpty
    private String email;

    @NotEmpty
    private String fullName;

    @NotEmpty
    private String password;
}
