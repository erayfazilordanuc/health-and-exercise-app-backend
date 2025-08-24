package exercise.Consent.services;

import java.util.Optional;

import exercise.Consent.dtos.ConsentDTO;
import exercise.Consent.dtos.UpsertConsentDTO;
import exercise.Consent.entities.Consent;
import exercise.Consent.enums.ConsentPurpose;
import exercise.User.entities.User;

public interface ConsentService {
  ConsentDTO upsertConsent(Long userId, UpsertConsentDTO dto, String ip, String ua);

  ConsentDTO approve(Long consentId, User actor);

  ConsentDTO withdraw(Long consentId, User actor); // actor: sahip mi? admin mi? kontrolü için

  ConsentDTO latest(Long userId, ConsentPurpose purpose);

  boolean hasAccepted(Long userId, ConsentPurpose purpose);
}