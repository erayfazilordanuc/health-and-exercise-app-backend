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
public class UpsertSymptomsDTO {

  private Integer pulse; // bpm
  private Integer steps; // steps count
  private Integer totalCaloriesBurned;
  private Integer activeCaloriesBurned;

  private Integer sleepMinutes; // sleepMinutes duration in minutes
  private List<String> sleepSessions; // JSON string olabilir (örneğin REM/DEEP uyku vs.)

  public UpsertSymptomsDTO(Symptoms symptom) {
    this.pulse = symptom.getPulse();
    this.steps = symptom.getSteps();
    this.sleepMinutes = symptom.getSleepMinutes();
  }
}
