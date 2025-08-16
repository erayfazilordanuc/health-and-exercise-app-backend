package exercise.Consent.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.UniqueConstraint;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import exercise.Consent.enums.ConsentPolicyPurpose;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "consent_policies", uniqueConstraints = @UniqueConstraint(columnNames = { "purpose", "version",
    "locale" }))
@Getter
@Setter
public class ConsentPolicy {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ConsentPolicyPurpose purpose;

  @Column(nullable = false, length = 16)
  private String version;

  @Column(nullable = false, length = 10)
  private String locale;

  @Column(nullable = false)
  private String contentHash;

  private String contentUrl;

  // 🔧 ÖNEMLİ: @Lob ve @Basic(LAZY) KALDIR
  @JdbcTypeCode(SqlTypes.LONGVARCHAR)
  @Column(columnDefinition = "text")
  private String contentMd;

  @CreationTimestamp
  @Column(updatable = false)
  private Timestamp createdAt;

  @Column(nullable = false)
  private Timestamp effectiveAt;
}
