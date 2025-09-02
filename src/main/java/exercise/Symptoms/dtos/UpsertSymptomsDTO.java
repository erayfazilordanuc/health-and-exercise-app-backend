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

  public UpsertSymptomsDTO(Symptoms symptoms) {
    this.pulse = symptoms.getPulse();
    this.steps = symptoms.getSteps();
    this.totalCaloriesBurned = symptoms.getTotalCaloriesBurned();
    this.activeCaloriesBurned = symptoms.getActiveCaloriesBurned();
    this.sleepMinutes = symptoms.getSleepMinutes();
  }
}
