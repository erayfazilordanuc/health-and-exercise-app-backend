package exercise.User.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateUserDTO {

    private Long id;

    private String username;

    private String email;

    private String password;

    private String fullName;

    private String height;

    private String weight;

    private Long groupId;

    private String theme;
}
