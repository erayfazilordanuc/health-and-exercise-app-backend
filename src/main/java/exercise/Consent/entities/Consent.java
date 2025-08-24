package exercise.Consent.entities;

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
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import exercise.Consent.enums.ConsentPurpose;
import exercise.Consent.enums.ConsentStatus;
import exercise.User.entities.User;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "consents", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "purpose" }))
@Getter
@Setter
public class Consent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "consent_policy_id")
  private ConsentPolicy consentPolicy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ConsentPurpose purpose;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ConsentStatus status; // ACCEPTED / REJECTED / WITHDRAWN

  @Column
  private String locale;
  @Column
  private String ipAddress;
  @Column
  private String userAgent;
  @Column
  private String source = "MOBILE";

  @CreationTimestamp
  @Column(updatable = false)
  private Timestamp createdAt;

  @UpdateTimestamp
  private Timestamp updatedAt;

  @Column
  private Timestamp grantedAt; // ilk ACCEPTED anı
  @Column
  private Timestamp withdrawnAt; // WITHDRAWN anı

  @JsonProperty("user_id")
  public Long getUserId() {
    return (user != null ? user.getId() : null);
  }
}