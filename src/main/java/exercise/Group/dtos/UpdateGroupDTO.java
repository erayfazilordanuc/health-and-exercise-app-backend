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
public class UpdateGroupDTO {

  @NotNull
  private Long id;

  private String name;

  private Boolean exerciseEnabled;
}