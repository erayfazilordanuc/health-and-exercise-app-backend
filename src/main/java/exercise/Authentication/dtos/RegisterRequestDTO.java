package exercise.Authentication.dtos;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {

    @NotNull
    private String username;

    // @NotNull It is not necessary due to email existence possibilities
    private String email;

    @NotNull
    private String fullName;

    @NotNull
    private LocalDate birthDate;

    @NotNull
    private String password;

    @NotNull
    private String gender;
}
