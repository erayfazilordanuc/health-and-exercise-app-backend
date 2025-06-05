package exercise.Authentication.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import exercise.User.entities.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {

    @NotEmpty
    private User user;

    @NotEmpty
    private String accessToken;

    // TO DO refresh token eklenebilir
}
