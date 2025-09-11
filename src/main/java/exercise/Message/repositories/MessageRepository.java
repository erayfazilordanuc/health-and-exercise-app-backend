package exercise.Message.repositories;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import exercise.Message.entities.Message;
import exercise.Symptoms.entities.Symptoms;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  public List<Message> findByRoomId(Long roomId);

  @Query("SELECT COALESCE(MAX(m.roomId), 0) FROM Message m")
  Long findLastRoomId();

  public List<Message> findBySender(String sender);

  public List<Message> findByReceiver(String receiver);

  public List<Message> findBySenderAndReceiver(String sender, String receiver);

  @Query("SELECT m FROM Message m WHERE (m.sender = :sender AND m.receiver = :receiver) OR (m.sender = :receiver AND m.receiver = :sender) ORDER BY m.createdAt DESC")
  List<Message> findBySenderAndReceiverOrderByCreatedAtDesc(String sender, String receiver);

  @Query("""
      SELECT m
      FROM   Message m
      WHERE ((m.sender   = :sender     AND m.receiver = :receiver)
         OR  (m.sender   = :receiver   AND m.receiver = :sender))
        AND  m.message   LIKE %:keyword%
        AND  m.createdAt >= :startOfDay
        AND  m.createdAt <  :endOfDay
      ORDER BY m.createdAt DESC
      """)
  Optional<Message> findFirstByMessageContainingAndCreatedAtBetweenOrderByCreatedAtDesc(
      String keyword, String sender, String receiver,
      LocalDateTime startOfDay,
      LocalDateTime endOfDay);

  boolean existsBySenderAndReceiverAndMessageStartingWithAndCreatedAtBetween(
      String sender, String receiver,
      String prefix,
      LocalDateTime start, LocalDateTime end);
}
