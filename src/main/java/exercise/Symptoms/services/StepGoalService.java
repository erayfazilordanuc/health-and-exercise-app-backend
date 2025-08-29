package exercise.Symptoms.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import exercise.Symptoms.dtos.StepGoalDTO;
import exercise.Symptoms.entities.StepGoal;
import exercise.Symptoms.entities.Symptoms;
import exercise.Symptoms.repositories.StepGoalRepository;
import exercise.User.entities.User;

@Service
public class StepGoalService {

  @Autowired
  private StepGoalRepository repo;

  @Autowired
  SymptomsService symptomsService;

  public StepGoalDTO create(Integer goalValue, User user) {
    ZoneId zone = ZoneId.of("Europe/Istanbul");
    LocalDate today = LocalDate.now(zone);

    LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
    LocalDate endOfWeek = startOfWeek.plusDays(7);

    LocalDateTime start = startOfWeek.atStartOfDay();
    LocalDateTime end = endOfWeek.atStartOfDay();
    Optional<StepGoal> goal = repo.findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(user.getId(), start, end);
    if (goal.isPresent())
      throw new RuntimeException("Goal already exist for this week");

    Integer weeklyStepAverage = symptomsService.getAverageWeeklyStepsExcludingCurrent(user.getId());

    if (goalValue < weeklyStepAverage - 5000)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Önceki haftadaki ilerlemenize göre bu kadar düşük bir hedef giremezsiniz");

    StepGoal newGoal = new StepGoal(null, user, goalValue, false, null, null);
    return new StepGoalDTO(repo.save(newGoal));
  }

  public StepGoalDTO complete(Long id, Long userId) {
    ZoneId zone = ZoneId.of("Europe/Istanbul");
    LocalDate today = LocalDate.now(zone);

    LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
    LocalDate endOfWeek = startOfWeek.plusDays(7);

    LocalDateTime start = startOfWeek.atStartOfDay();
    LocalDateTime end = endOfWeek.atStartOfDay();

    StepGoal goal = repo.findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, start, end)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step Goal Not Found"));

    goal.setIsDone(true);
    return new StepGoalDTO(repo.save(goal));
  }

  public StepGoalDTO getWeeklyByUserId(Long userId) {
    // Türkiye saat dilimi
    ZoneId zone = ZoneId.of("Europe/Istanbul");
    LocalDate today = LocalDate.now(zone);

    LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
    LocalDate endOfWeek = startOfWeek.plusDays(7);

    LocalDateTime start = startOfWeek.atStartOfDay();
    LocalDateTime end = endOfWeek.atStartOfDay();

    return new StepGoalDTO(repo.findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, start, end)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step Goal Not Found")));
  }

  public List<StepGoalDTO> getDonesByUserId(Long userId) {
    return repo.findAllByUserIdAndIsDoneTrueOrderByCreatedAtDesc(userId)
        .stream()
        .map(StepGoalDTO::new)
        .toList();
  }
}