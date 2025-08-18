package exercise.Consent.dtos;

import java.sql.Timestamp;

import exercise.Consent.entities.ConsentPolicy;

public record ConsentPolicyDTO(
    Long id,
    String purpose,
    String version,
    String locale,
    String contentHash,
    String contentUrl,
    String content, // includeContent=true ise dolar
    Timestamp createdAt,
    Timestamp effectiveAt) {

  public static ConsentPolicyDTO of(ConsentPolicy p, boolean includeContent) {
    return new ConsentPolicyDTO(
        p.getId(),
        p.getPurpose().name(), p.getVersion(), p.getLocale(),
        p.getContentHash(), p.getContentUrl(),
        includeContent ? p.getContentMd() : null, p.getCreatedAt(), p.getEffectiveAt());
  }
}
