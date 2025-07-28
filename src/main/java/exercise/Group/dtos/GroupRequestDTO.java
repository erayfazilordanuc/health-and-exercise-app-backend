package exercise.Group.dtos;

import exercise.User.dtos.UserDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequestDTO {

  private Long id;

  @NotNull
  private UserDTO userDTO;

  @NotNull
  private GroupDTO groupDTO;
}