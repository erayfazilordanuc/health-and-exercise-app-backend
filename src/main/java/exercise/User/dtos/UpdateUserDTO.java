package exercise.User.dtos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.Symptoms.entities.Symptoms;
import exercise.User.entities.User;
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

    private String fullName;

    private String password;

    private Long groupId;

    public UpdateUserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.groupId = user.getGroupId();
    }
}
