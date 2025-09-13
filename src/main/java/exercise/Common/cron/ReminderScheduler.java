package exercise.Common.cron;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import exercise.Exercise.entities.ExerciseSchedule;
import exercise.Exercise.entities.ExerciseVideoProgress;
import exercise.Exercise.repositories.ExerciseScheduleRepository;
import exercise.Exercise.repositories.ExerciseVideoProgressRepository;
import exercise.Group.entities.Group;
import exercise.Group.repositories.GroupRepository;
import exercise.Message.entities.Message;
import exercise.Message.services.MessageService;
import exercise.Notification.services.NotificationService;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReminderScheduler {
  private final UserRepository userRepo;
  private final ExerciseScheduleRepository exerciseScheduleRepo;
  private final ExerciseVideoProgressRepository exerciseVideoProgressRepo;
  private final NotificationService notificationService;
  private final MessageService messageService;
  private final GroupRepository groupRepo;

  @Scheduled(cron = "0 26 14 * * ?", zone = "Europe/Istanbul")
  public void sendMiddayExerciseReminder() {
    final ZoneId zone = ZoneId.of("Europe/Istanbul");
    final int todayIdx = ZonedDateTime.now(zone).getDayOfWeek().getValue();

    List<User> allUsers = userRepo.findAll();

    List<User> targetUsers = allUsers.stream().filter(u -> u.getRole().equals("ROLE_USER")).toList();

    LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
    Timestamp startTs = Timestamp.valueOf(startOfToday);
    Timestamp endTs = Timestamp.valueOf(startOfToday.plusDays(1));

    List<User> usersToRemind = targetUsers.stream()
        .filter(u -> {
          Optional<ExerciseSchedule> schedule = exerciseScheduleRepo.findByUserId(u.getId());
          if (schedule.isPresent()) {
            List<Long> activeDays = schedule.get().getActiveDays();
            if (activeDays == null || activeDays.isEmpty())
              return false;
            boolean isActiveToday = activeDays.contains((long) todayIdx);
            if (!isActiveToday)
              return false;
          } else
            return false;

          List<ExerciseVideoProgress> vp = exerciseVideoProgressRepo
              .findByUserIdAndCreatedAtBetween(u.getId(), startTs, endTs, Sort.by(Sort.Direction.ASC, "createdAt"));
          if (vp.isEmpty() || !vp.stream().allMatch(ExerciseVideoProgress::getIsCompeleted))
            return true;
          return false;
        })
        .toList();

    usersToRemind.forEach(user -> notificationService.sendExerciseReminderNotification(user));
  }

  @Scheduled(cron = "0 25 14 * * ?", zone = "Europe/Istanbul")
  public void sendDailyStatusReminder() {
    List<User> allUsers = userRepo.findAll();

    List<User> targetUsers = allUsers.stream()
        .filter(u -> "ROLE_USER".equals(u.getRole()))
        .toList();

    List<User> usersToRemind = targetUsers.stream()
        .filter(u -> {
          Long groupId = u.getGroupId();
          if (groupId == null)
            return false;

          Group group = groupRepo.findById(groupId).orElse(null);
          if (group == null)
            return false;

          Long adminId = group.getAdminId();
          if (adminId == null)
            return false;

          User admin = userRepo.findById(adminId).orElse(null);
          if (admin == null)
            return false;

          Message dailyStatusMessage = messageService.isDailyStatusExistForToday(u.getUsername(), admin.getUsername());
          return dailyStatusMessage == null;
        })
        .toList();

    usersToRemind.forEach(notificationService::sendDailyStatusReminderNotification);
  }

  // @Scheduled(cron = "0 * * * * *", zone = "Europe/Istanbul")
  public void testSendMiddayExerciseReminder() {
    final String runId = UUID.randomUUID().toString().substring(0, 8);
    final Logger log = LoggerFactory.getLogger(getClass());
    final StopWatch sw = new StopWatch("middayReminder-" + runId);
    final ZoneId zone = ZoneId.of("Europe/Istanbul");

    try {
      sw.start();
      final int todayIdx = ZonedDateTime.now(zone).getDayOfWeek().getValue();
      log.info("runId={} job=middayReminder start tz={} todayIdx={}", runId, zone, todayIdx);

      Set<String> allowed = Set.of("ordanuc", "test65");
      List<User> allUsers = userRepo.findAll();
      List<User> targetUsers = allUsers.stream()
          .filter(u -> allowed.contains(u.getUsername()))
          .filter(u -> "ROLE_USER".equals(u.getRole()))
          .toList();

      log.info("runId={} job=middayReminder candidates total={} allowedMatch={}",
          runId, allUsers.size(), targetUsers.size());

      // Bugünün başlangıcı ve sonu
      LocalDateTime startOfToday = LocalDate.now(zone).atStartOfDay();
      Timestamp startTs = Timestamp.valueOf(startOfToday);
      Timestamp endTs = Timestamp.valueOf(startOfToday.plusDays(1));

      List<User> usersToRemind = new ArrayList<>();
      for (User u : targetUsers) {
        String userLogPrefix = String.format("runId=%s job=middayReminder user=%s id=%s", runId, u.getUsername(),
            u.getId());

        Optional<ExerciseSchedule> scheduleOpt = exerciseScheduleRepo.findByUserId(u.getId());
        if (scheduleOpt.isEmpty()) {
          log.debug("{} skip=NO_SCHEDULE", userLogPrefix);
          continue;
        }
        List<Long> activeDays = scheduleOpt.get().getActiveDays();
        if (activeDays == null || activeDays.isEmpty()) {
          log.debug("{} skip=EMPTY_ACTIVE_DAYS", userLogPrefix);
          continue;
        }
        boolean isActiveToday = activeDays.contains((long) todayIdx);
        if (!isActiveToday) {
          log.debug("{} skip=NOT_ACTIVE_TODAY activeDays={}", userLogPrefix, activeDays);
          continue;
        }

        List<ExerciseVideoProgress> vp = exerciseVideoProgressRepo.findByUserIdAndCreatedAtBetween(
            u.getId(), startTs, endTs, Sort.by(Sort.Direction.ASC, "createdAt"));

        boolean allCompleted = !vp.isEmpty() && vp.stream().allMatch(ExerciseVideoProgress::getIsCompeleted);
        log.debug("{} progressCount={} allCompletedToday={}", userLogPrefix, vp.size(), allCompleted);

        if (vp.isEmpty() || !allCompleted) {
          usersToRemind.add(u);
          log.info("{} DECISION=REMIND", userLogPrefix);
        } else {
          log.debug("{} skip=ALREADY_COMPLETED", userLogPrefix);
        }
      }

      log.info("runId={} job=middayReminder toRemindCount={}", runId, usersToRemind.size());
      usersToRemind.forEach(notificationService::sendExerciseReminderNotification);
    } catch (Exception e) {
      log.error("runId={} job=middayReminder ERROR msg={}", runId, e.getMessage(), e);
    } finally {
      sw.stop();
      log.info("runId={} job=middayReminder end durationMs={}", runId, sw.getTotalTimeMillis());
    }
  }

  // @Scheduled(cron = "0 * * * * *", zone = "Europe/Istanbul")
  public void testSendDailyStatusReminder() {
    final String runId = UUID.randomUUID().toString().substring(0, 8);
    final Logger log = LoggerFactory.getLogger(getClass());
    final StopWatch sw = new StopWatch("dailyStatusReminder-" + runId);

    try {
      sw.start();
      log.info("runId={} job=dailyStatusReminder start tz=Europe/Istanbul", runId);

      Set<String> allowed = Set.of("ordanuc", "test65");
      List<User> allUsers = userRepo.findAll();
      List<User> targetUsers = allUsers.stream()
          .filter(u -> allowed.contains(u.getUsername()))
          .filter(u -> "ROLE_USER".equals(u.getRole()))
          .toList();

      log.info("runId={} job=dailyStatusReminder candidates total={} allowedMatch={}",
          runId, allUsers.size(), targetUsers.size());

      List<User> usersToRemind = new ArrayList<>();
      for (User u : targetUsers) {
        String userLogPrefix = String.format("runId=%s job=dailyStatusReminder user=%s id=%s", runId, u.getUsername(),
            u.getId());

        Group group = (u.getGroupId() != null) ? groupRepo.findById(u.getGroupId()).orElse(null) : null;
        if (group == null) {
          log.debug("{} skip=NO_GROUP", userLogPrefix);
          continue;
        }
        User admin = userRepo.findById(group.getAdminId()).orElse(null);
        if (admin == null) {
          log.debug("{} skip=NO_ADMIN groupId={}", userLogPrefix, group.getId());
          continue;
        }

        // Mevcut mantık: varsa true (hatırlatma gönder)
        Message dailyStatusMessage = messageService.isDailyStatusExistForToday(u.getUsername(), admin.getUsername());
        boolean exists = (dailyStatusMessage != null);
        log.debug("{} dailyStatusExists={}", userLogPrefix, exists);

        if (exists) {
          usersToRemind.add(u);
          log.info("{} DECISION=REMIND", userLogPrefix);
        } else {
          log.debug("{} skip=NO_DAILY_STATUS", userLogPrefix);
        }
      }

      log.info("runId={} job=dailyStatusReminder toRemindCount={}", runId, usersToRemind.size());
      usersToRemind.forEach(notificationService::sendDailyStatusReminderNotification);
    } catch (Exception e) {
      log.error("runId={} job=dailyStatusReminder ERROR msg={}", runId, e.getMessage(), e);
    } finally {
      sw.stop();
      log.info("runId={} job=dailyStatusReminder end durationMs={}", runId, sw.getTotalTimeMillis());
    }
  }

  // @Scheduled(cron = "0 35 12 ? * SUN", zone = "Europe/Istanbul")
  public void testReminderSchedule() {
    System.out.println("Test schedule is working");
    notificationService.sendTestReminderNotification(null);
  }

  // @Scheduled(cron = "0 * * * * *", zone = "Europe/Istanbul")
  public void testSchedule() {
    System.out.println("Test schedule is working");
    notificationService.sendTestReminderNotification(null);
  }
}
