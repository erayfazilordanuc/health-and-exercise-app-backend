package exercise.Authentication.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import exercise.Common.validation.login.ValidLoginRequestDTO;

@Getter
@Setter
// This validation is for username or email login options
public class LoginRequestDTO {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;
}
