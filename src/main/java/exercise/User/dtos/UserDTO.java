package exercise.User.dtos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.User.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;

    private String username;

    private String email;

    private String fullName;

    private LocalDate birthDate;

    private String gender;

    private String role;

    private List<SymptomsDTO> symptomList = new ArrayList<>();

    private Long groupId;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.birthDate = user.getBirthDate();
        this.gender = user.getGender();
        this.role = user.getRole();
        this.groupId = user.getGroupId();
    }
}
