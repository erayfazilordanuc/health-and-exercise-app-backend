package exercise.User.dtos;

import exercise.Symptoms.entities.Symptoms;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDTO {

    private String username;

    private String email;

    private String fullName;

    private String password;

    private Symptoms symptoms;

    public CharSequence getPassword() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
