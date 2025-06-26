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
public class UpdateSymptomsDTO {

  private Integer pulse; // bpm
  private Integer steps; // steps count
  private Integer activeCaloriesBurned;

  private Integer sleepHours; // sleep duration in minutes
  private List<String> sleepSessions; // JSON string olabilir (örneğin REM/DEEP uyku vs.)

  public UpdateSymptomsDTO(Symptoms symptom) {
    this.pulse = symptom.getPulse();
    this.steps = symptom.getSteps();
    this.sleepHours = symptom.getSleepHours();
    this.sleepSessions = symptom.getSleepSessions();
  }
}
