package exercise.User.dtos;

import java.util.List;
import exercise.Exercise.dtos.AchievementDTO;
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

    // private String password;

    private Long groupId;

    private List<AchievementDTO> achievementDTOs;
}
