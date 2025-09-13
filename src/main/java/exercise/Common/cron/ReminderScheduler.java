package exercise.Common.cron;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
          Group group = groupRepo.findById(u.getGroupId()).orElse(null);
          if (group == null)
            return false;
          User admin = userRepo.findById(group.getAdminId()).orElse(null);
          if (admin == null)
            return false;

          Message dailyStatusMessage = messageService.isDailyStatusExistForToday(u.getUsername(), admin.getUsername());
          return Objects.nonNull(dailyStatusMessage);
        })
        .toList();

    usersToRemind.forEach(user -> notificationService.sendDailyStatusReminderNotification(user));
  }

  @Scheduled(cron = "0 * * * * *", zone = "Europe/Istanbul")
  public void testSendMiddayExerciseReminder() {
    final ZoneId zone = ZoneId.of("Europe/Istanbul");
    final int todayIdx = ZonedDateTime.now(zone).getDayOfWeek().getValue();

    // Sadece test kullanıcıları
    Set<String> allowed = Set.of("ordanuc", "test65");
    List<User> targetUsers = userRepo.findAll()
        .stream()
        .filter(u -> allowed.contains(u.getUsername()))
        .filter(u -> "ROLE_USER".equals(u.getRole()))
        .toList();

    // Bugünün başlangıcı ve sonu
    LocalDateTime startOfToday = LocalDate.now(zone).atStartOfDay();
    Timestamp startTs = Timestamp.valueOf(startOfToday);
    Timestamp endTs = Timestamp.valueOf(startOfToday.plusDays(1));

    List<User> usersToRemind = targetUsers.stream()
        .filter(u -> {
          Optional<ExerciseSchedule> scheduleOpt = exerciseScheduleRepo.findByUserId(u.getId());
          if (scheduleOpt.isEmpty())
            return false;
          List<Long> activeDays = scheduleOpt.get().getActiveDays();
          if (activeDays == null || activeDays.isEmpty())
            return false;
          boolean isActiveToday = activeDays.contains((long) todayIdx);
          if (!isActiveToday)
            return false;

          List<ExerciseVideoProgress> vp = exerciseVideoProgressRepo
              .findByUserIdAndCreatedAtBetween(
                  u.getId(), startTs, endTs, Sort.by(Sort.Direction.ASC, "createdAt"));
          return vp.isEmpty() || !vp.stream().allMatch(ExerciseVideoProgress::getIsCompeleted);
        })
        .toList();

    usersToRemind.forEach(notificationService::sendExerciseReminderNotification);
  }

  // Her dakika, Europe/Istanbul
  @Scheduled(cron = "0 * * * * *", zone = "Europe/Istanbul")
  public void testSendDailyStatusReminder() {
    // Sadece test kullanıcıları
    Set<String> allowed = Set.of("ordanuc", "test65");
    List<User> targetUsers = userRepo.findAll()
        .stream()
        .filter(u -> allowed.contains(u.getUsername()))
        .filter(u -> "ROLE_USER".equals(u.getRole()))
        .toList();

    List<User> usersToRemind = targetUsers.stream()
        .filter(u -> {
          Group group = groupRepo.findById(u.getGroupId()).orElse(null);
          if (group == null)
            return false;
          User admin = userRepo.findById(group.getAdminId()).orElse(null);
          if (admin == null)
            return false;

          // Mevcut kodundaki mantığı aynen korudum:
          // Günlük durum mesajı VARSA true (hatırlatma gönder)
          Message dailyStatusMessage = messageService.isDailyStatusExistForToday(u.getUsername(), admin.getUsername());
          return Objects.nonNull(dailyStatusMessage);
        })
        .toList();

    usersToRemind.forEach(notificationService::sendDailyStatusReminderNotification);
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
