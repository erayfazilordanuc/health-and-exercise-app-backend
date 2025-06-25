package exercise.Authentication.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import exercise.User.dtos.UserDTO;
import exercise.User.entities.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {

    @NotEmpty
    private UserDTO userDTO;

    @NotEmpty
    private String accessToken;

    @NotEmpty
    private String refreshToken;

    // TO DO refresh token eklenebilir
}
