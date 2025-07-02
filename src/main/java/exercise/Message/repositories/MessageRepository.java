package exercise.Message.repositories;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import exercise.Message.entities.Message;
import exercise.Symptoms.entities.Symptoms;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  public List<Message> findByRoomId(Long roomId);

  public List<Message> findBySender(String sender);

  public List<Message> findByReceiver(String receiver);
}
