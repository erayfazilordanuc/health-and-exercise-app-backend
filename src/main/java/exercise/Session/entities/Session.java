package exercise.Session.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.UniqueConstraint;

import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import exercise.User.entities.User;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sessions", uniqueConstraints = @UniqueConstraint(columnNames = { "session_id" }))
@Getter
@Setter
public class Session {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private User user;

  @Column(nullable = false, unique = true)
  private UUID sessionId;

  @Column(nullable = false)
  private Timestamp startedAt;

  @Column
  private Timestamp endedAt;

  @Column(nullable = false)
  private Long activeMs = 0L;

  @Column
  private Timestamp lastHeartbeatAt;

  @Column(nullable = false)
  private Integer heartbeatCount = 0;

  @Column
  private String reason; // "logout" | "close"

  @Column
  private String source = "MOBILE";

  @CreationTimestamp
  @Column(updatable = false)
  private Timestamp createdAt;

  @UpdateTimestamp
  @Column
  private Timestamp updatedAt;

  @JsonProperty("user_id")
  public Long getUserId() {
    return (user != null ? user.getId() : null);
  }
}
