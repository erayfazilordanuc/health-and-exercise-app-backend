package exercise.User.dtos;

import java.math.BigDecimal;
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

    private BigDecimal height;

    private BigDecimal weight;

    private Long groupId;

    private String theme;
}
