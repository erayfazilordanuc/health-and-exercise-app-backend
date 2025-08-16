package exercise.Consent.dtos;

import exercise.Consent.entities.ConsentPolicy;

public record PolicyResponse(
    String purpose,
    String version,
    String locale,
    String contentHash,
    String contentUrl,
    String content // includeContent=true ise dolar
) {
  public static PolicyResponse of(ConsentPolicy p, boolean includeContent) {
    return new PolicyResponse(
        p.getPurpose().name(), p.getVersion(), p.getLocale(),
        p.getContentHash(), p.getContentUrl(),
        includeContent ? p.getContentMd() : null);
  }
}
