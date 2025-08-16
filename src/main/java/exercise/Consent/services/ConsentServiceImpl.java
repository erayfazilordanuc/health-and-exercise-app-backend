package exercise.Consent.services;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import exercise.Consent.dtos.ConsentDTO;
import exercise.Consent.entities.Consent;
import exercise.Consent.enums.ConsentPurpose;
import exercise.Consent.enums.ConsentStatus;
import exercise.Consent.repositories.ConsentRepository;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsentServiceImpl implements ConsentService {

  private final ConsentRepository repo;
  private final UserRepository userRepo; // sadece id'li proxy yaratmak istemezsen

  @Transactional
  @Override
  public Consent upsertConsent(Long userId, ConsentDTO dto, String ip, String ua) {
    Consent c = repo.findByUser_IdAndPurpose(userId, dto.purpose())
        .orElseGet(() -> {
          Consent n = new Consent();
          // sadece id veren proxy yeterliyse:
          User u = new User();
          u.setId(userId);
          n.setUser(u);
          n.setPurpose(dto.purpose());
          return n;
        });

    c.setStatus(dto.status());
    c.setPolicyVersion(dto.policyVersion());
    c.setEvidenceHash(dto.evidenceHash());
    c.setIpAddress(ip);
    c.setUserAgent(ua);
    c.setLocale(dto.locale());
    if (dto.source() != null && !dto.source().isBlank()) {
      c.setSource(dto.source());
    }
    // grantedAt/withdrawnAt gibi alanlar eklediysen @PrePersist/@PreUpdate ile
    // otomatik işler
    return repo.save(c);
  }

  @Transactional
  @Override
  public Consent withdraw(Long consentId, User actor) {
    Consent c = repo.findById(consentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CONSENT_NOT_FOUND"));

    boolean isOwner = c.getUser().getId().equals(actor.getId());
    boolean isAdmin = actor.getRole() != null && actor.getRole().equals("ROLE_ADMIN");
    if (!isOwner && !isAdmin) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT_ALLOWED");
    }

    // zaten withdrawn ise dokunma
    if (c.getStatus() != ConsentStatus.WITHDRAWN) {
      c.setStatus(ConsentStatus.WITHDRAWN);
      // varsa lifecycle hook'ların withdrawnAt'i setler
      c = repo.save(c);
    }
    return c;
  }

  @Override
  public Optional<Consent> latest(Long userId, ConsentPurpose purpose) {
    return repo.findByUser_IdAndPurpose(userId, purpose);
  }

  @Override
  public boolean hasAccepted(Long userId, ConsentPurpose purpose) {
    return repo.findByUser_IdAndPurpose(userId, purpose)
        .map(c -> c.getStatus() == ConsentStatus.ACCEPTED)
        .orElse(false);
  }
}