package exercise.Consent.services;

import java.sql.Timestamp;
import java.util.Optional;

import org.checkerframework.checker.units.qual.m;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import exercise.Consent.dtos.ConsentDTO;
import exercise.Consent.dtos.ConsentPolicyDTO;
import exercise.Consent.dtos.UpsertConsentDTO;
import exercise.Consent.entities.Consent;
import exercise.Consent.entities.ConsentPolicy;
import exercise.Consent.enums.ConsentPurpose;
import exercise.Consent.enums.ConsentStatus;
import exercise.Consent.mappers.ConsentMapper;
import exercise.Consent.repositories.ConsentPolicyRepository;
import exercise.Consent.repositories.ConsentRepository;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsentServiceImpl implements ConsentService {

  private final ConsentRepository repo;
  private final ConsentPolicyRepository policyRepo;
  private final ConsentMapper mapper;

  @Transactional
  @Override
  public Consent upsertConsent(Long userId, UpsertConsentDTO dto, String ip, String ua) {
    Consent c = repo.findByUser_IdAndPurpose(userId, dto.getPurpose())
        .orElseGet(() -> {
          Consent n = new Consent();
          User u = new User();
          u.setId(userId);
          n.setUser(u);
          n.setPurpose(dto.getPurpose());
          return n;
        });

    ConsentPolicy consentPolicy = policyRepo.findById(dto.getPolicyId()).get();
    if (dto.getPurpose() == ConsentPurpose.KVKK_NOTICE_ACK) {
      c.setStatus(ConsentStatus.ACKNOWLEDGED); // gelen status'ü YOK SAY
      if (c.getGrantedAt() == null) {
        c.setGrantedAt(new Timestamp(System.currentTimeMillis()));
      }
      c.setWithdrawnAt(null);
    } else {
      c.setStatus(dto.getStatus());
    }
    c.setConsentPolicy(consentPolicy);
    c.setIpAddress(ip);
    c.setUserAgent(ua);
    c.setLocale(dto.getLocale());
    if (dto.getSource() != null && !dto.getSource().isBlank()) {
      c.setSource(dto.getSource());
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

  @Transactional
  @Override
  public ConsentDTO latest(Long userId, ConsentPurpose purpose) {
    Optional<Consent> consent = repo.findByUser_IdAndPurpose(userId, purpose);
    if (consent.isPresent()) {
      ConsentDTO dto = mapper.entityToDTO(consent.get());
      return dto;
    } else
      return null;
  }

  @Override
  public boolean hasAccepted(Long userId, ConsentPurpose purpose) {
    return repo.findByUser_IdAndPurpose(userId, purpose)
        .map(c -> c.getStatus() == ConsentStatus.ACCEPTED)
        .orElse(false);
  }
}