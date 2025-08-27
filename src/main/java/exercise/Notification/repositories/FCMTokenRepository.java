package exercise.Notification.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exercise.Notification.entities.FCMToken;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {

  public List<FCMToken> findByUserId(Long userId);

  public Optional<FCMToken> findByToken(String token);
}