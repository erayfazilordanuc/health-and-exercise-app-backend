package exercise.Authentication.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ForgotPasswordRequestDTO {
    private String email;
}
