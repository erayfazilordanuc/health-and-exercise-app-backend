package exercise.Exercise.services;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import exercise.Notification.services.NotificationService;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class ReminderScheduler {
//   private final UserRepository userRepo; // dependency‑ler
//   private final NotificationService notificationService;

//   @Scheduled(cron = "0 0 12 ? * MON,WED,FRI", zone = "Europe/Istanbul")

//   public void sendMiddayExerciseReminder() {

//     // List<User> targets = userRepo.findAllByRole("ROLE_USER");

//     // burada egzersizi tamamlayanları elesin

//     // Burada fcm tokenlerin listesi çekilsin

//     // targets.forEach(user -> notificationService.sendNotification(
//     // user.getFcmToken(),
//     // "Egzersiz zamanı!",
//     // "Bugünkü antrenmanını kaçırma 💪"));
//   }
// }
