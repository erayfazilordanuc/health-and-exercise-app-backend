package exercise.Consent.services;

import java.util.Optional;

import exercise.Consent.dtos.ConsentDTO;
import exercise.Consent.entities.Consent;
import exercise.Consent.enums.ConsentPurpose;
import exercise.User.entities.User;

public interface ConsentService {
  Consent upsertConsent(Long userId, ConsentDTO dto, String ip, String ua);

  Consent withdraw(Long consentId, User actor); // actor: sahip mi? admin mi? kontrolü için

  Optional<Consent> latest(Long userId, ConsentPurpose purpose);

  boolean hasAccepted(Long userId, ConsentPurpose purpose);
}