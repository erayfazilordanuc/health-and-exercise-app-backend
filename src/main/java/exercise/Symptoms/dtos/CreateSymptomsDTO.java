package exercise.Symptoms.dtos;

import java.sql.Timestamp;
import java.util.List;

import exercise.Symptoms.entities.Symptoms;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateSymptomsDTO {

  private Integer pulse; // bpm
  private Integer steps; // steps count
  private Integer sleep; // sleep duration in minutes

  private List<String> sleepSession; // JSON string olabilir (örneğin REM/DEEP uyku vs.)

  private Long userId;

  public CreateSymptomsDTO(Symptoms symptom) {
    this.pulse = symptom.getPulse();
    this.steps = symptom.getSteps();
    this.sleep = symptom.getSleep();
    this.sleepSession = symptom.getSleepSession();
    this.userId = symptom.getUser() != null ? symptom.getUser().getId() : null;
  }
}
