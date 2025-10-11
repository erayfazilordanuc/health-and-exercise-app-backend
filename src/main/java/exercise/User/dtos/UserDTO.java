package exercise.User.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.User.entities.User;
import jakarta.persistence.Column;
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

    private BigDecimal height;

    private BigDecimal weight;

    private String role;

    // private List<SymptomsDTO> symptomList = new ArrayList<>();

    private Long groupId;

    private String theme;

    private String avatar;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.birthDate = user.getBirthDate();
        this.gender = user.getGender();
        this.height = user.getHeight();
        this.weight = user.getWeight();
        this.role = user.getRole();
        this.groupId = user.getGroupId();
        this.theme = user.getTheme();
        this.avatar = user.getAvatar();
    }
}
