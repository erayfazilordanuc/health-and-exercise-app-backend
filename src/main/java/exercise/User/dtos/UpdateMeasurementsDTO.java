package exercise.User.dtos;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateMeasurementsDTO {

    private BigDecimal height;

    private BigDecimal weight;
}
