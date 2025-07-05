package exercise.Notification.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exercise.Notification.entities.FCMToken;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {

  public List<FCMToken> findByUserId(Long userId);
}