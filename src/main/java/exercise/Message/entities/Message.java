package exercise.Message.entities;

import java.sql.Timestamp;
import java.util.Objects;

import org.checkerframework.checker.units.qual.C;
import org.hibernate.annotations.CreationTimestamp;

import exercise.Message.dtos.MessageDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "messages")
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String receiver;

    @Column(nullable = false)
    private Long roomId;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    public Message(
            MessageDTO messageDTO) {
        this.id = Objects.isNull(messageDTO.getId()) ? null : messageDTO.getId();
        this.message = messageDTO.getMessage();
        this.sender = messageDTO.getSender();
        this.receiver = messageDTO.getReceiver();
        this.roomId = messageDTO.getRoomId();
        this.createdAt = Objects.isNull(messageDTO.getCreatedAt()) ? null : messageDTO.getCreatedAt();
    }
}