package exercise.Consent.dtos;

import java.sql.Timestamp;

import exercise.Consent.entities.ConsentPolicy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ConsentPolicyDTO {
  private Long id;
  private String purpose;
  private String version;
  private String locale;
  private String contentHash;
  private String contentUrl;
  private String content; // includeContent=true ise dolar
  private Timestamp createdAt;
  private Timestamp effectiveAt;

  public ConsentPolicyDTO(ConsentPolicy p, boolean includeContent) {
    this.id = p.getId();
    this.purpose = p.getPurpose().name();
    this.version = p.getVersion();
    this.locale = p.getLocale();
    this.contentHash = p.getContentHash();
    this.contentUrl = p.getContentUrl();
    this.content = includeContent ? p.getContentMd() : null;
    this.createdAt = p.getCreatedAt();
    this.effectiveAt = p.getEffectiveAt();
  }
}