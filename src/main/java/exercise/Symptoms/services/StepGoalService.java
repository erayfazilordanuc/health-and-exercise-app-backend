package exercise.Symptoms.services;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import exercise.Symptoms.dtos.StepGoalDTO;
import exercise.Symptoms.entities.StepGoal;
import exercise.Symptoms.repositories.StepGoalRepository;
import exercise.User.entities.User;

@Service
public class StepGoalService {

  private static final ZoneId TR_ZONE = ZoneId.of("Europe/Istanbul");

  @Autowired
  private StepGoalRepository repo;

  @Autowired
  private SymptomsService symptomsService;

  /**
   * Verilen tarihin içinde bulunduğu haftanın (Pazartesi 00:00 - Pazar 23:59)
   * başlangıç ve bitişini Instant (UTC) olarak döndürür.
   */
  private TimeRange getCurrentWeekRange() {
    LocalDate today = LocalDate.now(TR_ZONE);
    LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
    LocalDate endOfWeek = startOfWeek.plusDays(6);

    ZonedDateTime startZoned = startOfWeek.atStartOfDay(TR_ZONE);
    ZonedDateTime endZoned = endOfWeek.atTime(LocalTime.MAX).atZone(TR_ZONE);

    return new TimeRange(startZoned.toInstant(), endZoned.toInstant());
  }

  public StepGoalDTO create(Integer goalValue, User user) {

    TimeRange weekRange = getCurrentWeekRange();

    Optional<StepGoal> goal = repo.findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        user.getId(), weekRange.start(), weekRange.end());

    if (goal.isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu hafta için zaten bir hedef mevcut.");
    }

    Integer weeklyStepAverage = symptomsService.getAverageWeeklyStepsExcludingCurrent(user.getId());

    if (goalValue < weeklyStepAverage - 5000)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Hedef, önceki ilerlemenize göre çok düşük.");

    if (weeklyStepAverage == 0) {
      int thisWeek = symptomsService.getThisWeekTotalSteps(user.getId());
      if (goalValue < thisWeek)
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Hedef, önceki ilerlemenize göre çok düşük.");
    }

    StepGoal newGoal = new StepGoal(null, user, goalValue, false, null, null);
    return new StepGoalDTO(repo.save(newGoal));
  }

  public StepGoalDTO complete(Long id, Long userId) {

    StepGoal goal = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step Goal Not Found"));

    if (!goal.getUser().getId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu hedefi güncelleme yetkiniz yok.");
    }

    goal.setIsDone(true);
    return new StepGoalDTO(repo.save(goal));
  }

  public StepGoalDTO getWeeklyStepGoalByUserId(Long userId) {
    ZoneId zone = ZoneId.of("Europe/Istanbul");
    LocalDate today = LocalDate.now(zone);

    LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
    LocalDate endOfWeek = startOfWeek.plusDays(7);

    Instant startRange;
    Instant endRange;

    startRange = startOfWeek.atStartOfDay(TR_ZONE).toInstant();
    endRange = endOfWeek.atTime(LocalTime.MAX).atZone(TR_ZONE).toInstant();

    return new StepGoalDTO(repo.findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startRange, endRange)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step Goal Not Found")));
  }

  public StepGoalDTO getWeeklyStepGoalInRangeForUser(Long userId, LocalDate startDate, LocalDate endDate) {
    Instant startRange;
    Instant endRange;

    if (startDate != null && endDate != null) {
      startRange = startDate.atStartOfDay(TR_ZONE).toInstant();
      endRange = endDate.atTime(LocalTime.MAX).atZone(TR_ZONE).toInstant();
    } else {
      TimeRange weekRange = getCurrentWeekRange();
      startRange = weekRange.start();
      endRange = weekRange.end();
    }

    return new StepGoalDTO(repo.findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startRange, endRange)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step Goal Not Found")));
  }

  public List<StepGoalDTO> getDonesByUserId(Long userId) {
    return repo.findAllByUserIdAndIsDoneTrueOrderByCreatedAtDesc(userId)
        .stream()
        .map(StepGoalDTO::new)
        .toList();
  }

  public List<StepGoalDTO> getDonesByUserIdUpToDate(Long userId, LocalDate endDate) {

    Instant endOfDay = endDate.atTime(LocalTime.MAX).atZone(TR_ZONE).toInstant();

    return repo.findAllByUserIdAndIsDoneTrueAndCreatedAtLessThanEqualOrderByCreatedAtDesc(userId, endOfDay)
        .stream()
        .map(StepGoalDTO::new)
        .toList();
  }

  private record TimeRange(Instant start, Instant end) {
  }
}