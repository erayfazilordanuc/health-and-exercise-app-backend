package exercise.Symptoms.dtos;

import java.sql.Timestamp;

import exercise.Symptoms.entities.StepGoal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StepGoalDTO {

  private Long id; // sleep duration in minutes

  private Long userId;

  private Integer goal;

  private Boolean isDone;

  private Timestamp createdAt;
  private Timestamp updatedAt;

  public StepGoalDTO(StepGoal stepGoal) {
    this.id = stepGoal.getId();
    this.userId = stepGoal.getUser() != null ? stepGoal.getUser().getId() : null;
    this.goal = stepGoal.getGoal();
    this.isDone = stepGoal.getIsDone();
    this.createdAt = stepGoal.getCreatedAt();
    this.updatedAt = stepGoal.getUpdatedAt();
  }
}
