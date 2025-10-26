package exercise.Symptoms.services;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import exercise.Symptoms.dtos.StepGoalDTO;
import exercise.Symptoms.entities.StepGoal;
import exercise.Symptoms.repositories.StepGoalRepository;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;

@Service
public class StepGoalService {

  private static final Logger logger = LoggerFactory.getLogger(StepGoalService.class);

  @Autowired
  private StepGoalRepository repo;

  @Autowired
  private SymptomsService symptomsService;

  @Autowired
  private UserRepository userRepository;

  private ZoneId getUserZoneId(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          logger.error("User not found with ID: {}", userId);
          return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId);
        });

    String userLocaleString = user.getLocale();

    if (userLocaleString == null || userLocaleString.isBlank()) {
      logger.warn("User {} has no locale set. Defaulting to UTC.", userId);
      return ZoneId.of("UTC");
    }

    try {
      Locale userLocale = Locale.forLanguageTag(userLocaleString.replace("_", "-"));
      String countryCode = userLocale.getCountry();

      if (countryCode == null || countryCode.isBlank()) {
        logger.warn("User {} locale '{}' has no country code. Defaulting to UTC.", userId, userLocaleString);
        return ZoneId.of("UTC");
      }

      Set<String> allZoneIds = ZoneId.getAvailableZoneIds();
      Optional<String> foundZoneId = allZoneIds.stream()
          .filter(id -> id.contains("/") && id.toUpperCase().contains(countryCode.toUpperCase()))
          .sorted()
          .findFirst();

      if (foundZoneId.isEmpty()) {
        foundZoneId = allZoneIds.stream()
            .filter(id -> id.toUpperCase().startsWith(getRegionForCountry(countryCode).toUpperCase()))
            .sorted()
            .findFirst();
      }

      if (foundZoneId.isPresent()) {
        String zoneIdStr = foundZoneId.get();
        logger.debug("Resolved ZoneId '{}' for User {} from locale '{}'", zoneIdStr, userId, userLocaleString);
        return ZoneId.of(zoneIdStr);
      } else {
        logger.warn(
            "Could not determine a specific ZoneId for country code '{}' from locale '{}' for User {}. Defaulting to UTC.",
            countryCode, userLocaleString, userId);
        return ZoneId.of("UTC");
      }

    } catch (Exception e) {
      logger.error("Error parsing locale '{}' or finding ZoneId for User {}. Defaulting to UTC. Error: {}",
          userLocaleString, userId, e.getMessage());
      return ZoneId.of("UTC");
    }
  }

  private String getRegionForCountry(String countryCode) {
    switch (countryCode.toUpperCase()) {
      case "TR":
        return "Europe";
      case "US":
        return "America";
      case "GB":
        return "Europe";
      case "DE":
        return "Europe";
      default:
        return "";
    }
  }

  public StepGoalDTO getWeeklyStepGoalByUserId(Long userId) {
    Timestamp startRange;
    Timestamp endRange;
    ZoneId userZone = getUserZoneId(userId);

    TimeRange weekRange = getCurrentWeekRangeAsTimestamp(userId);
    startRange = weekRange.start();
    endRange = weekRange.end();

    return new StepGoalDTO(repo.findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startRange, endRange)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step Goal Not Found")));
  }

  public StepGoalDTO getWeeklyStepGoalByUserId(Long userId, LocalDate startDate, LocalDate endDate) {
    Timestamp startRange;
    Timestamp endRange;
    ZoneId userZone = getUserZoneId(userId);

    if (startDate != null && endDate != null) {
      startRange = Timestamp.from(startDate.atStartOfDay(userZone).toInstant());
      endRange = Timestamp.from(endDate.atTime(LocalTime.MAX).atZone(userZone).toInstant());
    } else {
      TimeRange weekRange = getCurrentWeekRangeAsTimestamp(userId);
      startRange = weekRange.start();
      endRange = weekRange.end();
    }

    return new StepGoalDTO(repo.findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startRange, endRange)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step Goal Not Found")));
  }

  /**
   * Verilen tarihin içinde bulunduğu haftanın (Pazartesi 00:00 - Pazar 23:59)
   * başlangıç ve bitişini Timestamp (veritabanı tipi) olarak döndürür.
   */
  private TimeRange getCurrentWeekRangeAsTimestamp(Long userId) {
    ZoneId userZone = getUserZoneId(userId);
    LocalDate today = LocalDate.now(userZone);
    LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
    LocalDate endOfWeek = startOfWeek.plusDays(6);

    ZonedDateTime startZoned = startOfWeek.atStartOfDay(userZone);
    ZonedDateTime endZoned = endOfWeek.atTime(LocalTime.MAX).atZone(userZone);

    Timestamp startTimestamp = Timestamp.from(startZoned.toInstant());
    Timestamp endTimestamp = Timestamp.from(endZoned.toInstant());

    return new TimeRange(startTimestamp, endTimestamp);
  }

  public StepGoalDTO create(Integer goalValue, User user) {
    Long userId = user.getId();
    TimeRange weekRange = getCurrentWeekRangeAsTimestamp(userId);

    Optional<StepGoal> goal = repo.findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        userId, weekRange.start(), weekRange.end());

    if (goal.isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu hafta için zaten bir hedef mevcut.");
    }

    try {
      Integer weeklyStepAverage = symptomsService.getAverageWeeklyStepsExcludingCurrent(userId);

      int avgSteps = (weeklyStepAverage != null) ? weeklyStepAverage : 0;

      if (goalValue < avgSteps - 5000) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Hedef, önceki ilerlemenize göre çok düşük.");
      }

      if (avgSteps == 0) {
        int thisWeek = symptomsService.getThisWeekTotalSteps(userId);
        if (goalValue < thisWeek) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Hedef, önceki ilerlemenize göre çok düşük.");
        }
      }
    } catch (Exception e) {
      logger.error("Error checking step average for user {}: {}", userId, e.getMessage());
    }

    StepGoal newGoal = new StepGoal(null, user, goalValue, false, null, null);
    return new StepGoalDTO(repo.save(newGoal));
  }

  public StepGoalDTO complete(Long id, Long userId) {
    StepGoal goal = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step Goal Not Found with ID: " + id));

    if (!goal.getUser().getId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu hedefi güncelleme yetkiniz yok.");
    }

    if (goal.getIsDone() != null && goal.getIsDone()) {
      logger.info("Step goal with ID {} for user {} is already completed.", id, userId);
      return new StepGoalDTO(goal);
    }

    goal.setIsDone(true);
    return new StepGoalDTO(repo.save(goal));
  }

  public StepGoalDTO getWeeklyStepGoalInRangeForUser(Long userId, LocalDate startDate, LocalDate endDate) {
    Timestamp startRange;
    Timestamp endRange;
    ZoneId userZone = getUserZoneId(userId);

    if (startDate != null && endDate != null) {
      startRange = Timestamp.from(startDate.atStartOfDay(userZone).toInstant());
      endRange = Timestamp.from(endDate.atTime(LocalTime.MAX).atZone(userZone).toInstant());
    } else {
      TimeRange weekRange = getCurrentWeekRangeAsTimestamp(userId);
      startRange = weekRange.start();
      endRange = weekRange.end();
    }

    StepGoal result = new StepGoal();

    List<StepGoal> goals = repo.findByUserId(userId);

    for (StepGoal goal : goals) {
      if (goal.getUpdatedAt().after(endRange) && goal.getUpdatedAt().before(endRange)) {
        result = goal;
      }
    }

    return new StepGoalDTO(result);

    // return new
    // StepGoalDTO(repo.findTopByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId,
    // startRange, endRange)
    // .orElseThrow(() -> {
    // logger.warn("Step Goal Not Found for user {} in range {} - {}", userId,
    // startRange, endRange);
    // return new ResponseStatusException(HttpStatus.NOT_FOUND, "Step Goal Not
    // Found");
    // }));
  }

  public List<StepGoalDTO> getDonesByUserId(Long userId) {
    return repo.findAllByUserIdAndIsDoneTrueOrderByCreatedAtDesc(userId)
        .stream()
        .map(StepGoalDTO::new)
        .toList();
  }

  public List<StepGoalDTO> getDonesByUserIdUpToDate(Long userId, LocalDate endDate) {
    ZoneId userZone = getUserZoneId(userId);
    Timestamp endOfDay = Timestamp.from(endDate.atTime(LocalTime.MAX).atZone(userZone).toInstant());

    return repo.findAllByUserIdAndIsDoneTrueAndCreatedAtLessThanEqualOrderByCreatedAtDesc(userId, endOfDay)
        .stream()
        .map(StepGoalDTO::new)
        .toList();
  }

  private record TimeRange(Timestamp start, Timestamp end) {
  }
}