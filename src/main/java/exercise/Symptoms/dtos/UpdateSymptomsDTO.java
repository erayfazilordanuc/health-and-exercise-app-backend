package exercise.Symptoms.dtos;

import java.sql.Timestamp;

import exercise.Symptoms.entities.Symptoms;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateSymptomsDTO {

  private Integer pulse; // bpm
  private Integer steps; // steps count
  private Integer sleep; // sleep duration in minutes

  private String sleepSession; // JSON string olabilir (örneğin REM/DEEP uyku vs.)

  public UpdateSymptomsDTO(Symptoms symptom) {
    this.pulse = symptom.getPulse();
    this.steps = symptom.getSteps();
    this.sleep = symptom.getSleep();
    this.sleepSession = symptom.getSleepSession();
  }
}
