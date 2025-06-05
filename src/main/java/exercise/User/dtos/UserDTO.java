package exercise.User.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDTO {

    private String username;

    private String email;

    private String password;

    public CharSequence getPassword() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
