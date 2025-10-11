package exercise.Authentication.dtos;

import exercise.User.dtos.UserDTO;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponseDTO {

    @NotEmpty
    private UserDTO userDTO;

    @NotEmpty
    private String accessToken;

    @NotEmpty
    private String refreshToken;

    // TO DO refresh token eklenebilir
}
