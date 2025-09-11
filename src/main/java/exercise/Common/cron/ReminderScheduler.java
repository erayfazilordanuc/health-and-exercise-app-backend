package exercise.Common.cron;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

@Service
@RequiredArgsConstructor
public class ReminderScheduler {
  private final UserRepository userRepo;
  private final ExerciseScheduleRepository exerciseScheduleRepo;
  private final ExerciseVideoProgressRepository exerciseVideoProgressRepo;
  private final NotificationService notificationService;
  private final MessageService messageService;
  private final GroupRepository groupRepo;

  @Scheduled(cron = "0 0 12 * * ?", zone = "Europe/Istanbul")
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

  @Scheduled(cron = "0 0 12 * * *", zone = "Europe/Istanbul")
  public void sendDailyStatusReminder() {
    List<User> allUsers = userRepo.findAll();

    List<User> targetUsers = allUsers.stream().filter(u -> u.getRole().equals("ROLE_USER")).toList();

    List<User> usersToRemind = targetUsers.stream()
        .filter(u -> {
          Group group = groupRepo.findById(u.getGroupId()).get();
          User admin = userRepo.findById(group.getAdminId()).get();
          Message dailyStatusMessage = messageService.isDailyStatusExistForToday(u.getUsername(), admin.getUsername());
          if (Objects.nonNull(dailyStatusMessage))
            return true;
          else
            return false;
        })
        .toList();

    usersToRemind.forEach(user -> notificationService.sendDailyStatusReminderNotification(user));
  }

  // @Scheduled(cron = "0 0 12 ? * MON,WED,FRI", zone = "Europe/Istanbul")
  // public void sendMiddayExerciseReminder() {
  // List<User> allUsers = userRepo.findAll();

  // List<User> targetUsers = allUsers.stream().filter(u ->
  // u.getRole().equals("ROLE_USER")).toList();

  // LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
  // Timestamp startTs = Timestamp.valueOf(startOfToday);
  // Timestamp endTs = Timestamp.valueOf(startOfToday.plusDays(1));

  // List<User> usersToRemind = targetUsers.stream()
  // .filter(u -> {
  // List<ExerciseVideoProgress> vp = exerciseVideoProgressRepo
  // .findByUserIdAndCreatedAtBetween(u.getId(), startTs, endTs,
  // Sort.by(Sort.Direction.ASC, "createdAt"));
  // return vp.isEmpty() ||
  // !vp.stream().allMatch(ExerciseVideoProgress::getIsCompeleted);
  // })
  // .toList();

  // usersToRemind.forEach(user ->
  // notificationService.sendReminderNotification(user));
  // }

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
