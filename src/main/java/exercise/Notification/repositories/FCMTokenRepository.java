package exercise.Notification.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exercise.Notification.entities.FCMToken;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {

  public FCMToken findByUserId(Long userId);
}