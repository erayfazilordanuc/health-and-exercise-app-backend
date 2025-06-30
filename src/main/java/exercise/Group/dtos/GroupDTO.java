package exercise.Group.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {

  @NotNull
  private Long id;

  @NotNull
  private String name;

  @NotNull
  private Long adminId;
}