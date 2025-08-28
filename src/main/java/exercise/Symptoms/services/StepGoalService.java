package exercise.Symptoms.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exercise.Symptoms.entities.StepGoal;
import exercise.Symptoms.repositories.StepGoalRepository;
import exercise.User.entities.User;

@Service
public class StepGoalService {

  @Autowired
  private StepGoalRepository repo;

  public StepGoal create(Integer goal, User user) {
    StepGoal newGoal = new StepGoal(null, user, goal, false, null, null);
    return repo.save(newGoal);
  }

  public StepGoal complete(Long userId) {
    StepGoal goal = repo.findByUserId(userId);
    goal.setIsDone(true);
    return repo.save(goal);
  }

  public StepGoal getByUserId(Long userId) {
    return repo.findByUserId(userId);
  }
}