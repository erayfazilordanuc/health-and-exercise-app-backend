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
  private final UserRepository userRepo;

  @Transactional
  @Override
  public Consent upsertConsent(Long userId, UpsertConsentDTO dto, String ip, String ua) {
    Consent c = repo.findByUser_IdAndPurpose(userId, dto.getPurpose())
        .orElseGet(() -> {
          Consent n = new Consent();
          User u = userRepo.findById(userId).get();
          n.setUser(u);
          n.setPurpose(dto.getPurpose());
          return n;
        });

    ConsentPolicy consentPolicy = policyRepo.findById(dto.getPolicyId()).get();
    if (dto.getPurpose() == ConsentPurpose.KVKK_NOTICE_ACK) {
      c.setStatus(ConsentStatus.ACKNOWLEDGED); // gelen status'ü YOK SAY
    } else {
      c.setStatus(dto.getStatus());
    }
    syncTimestampsForStatus(c); // <-- EKLE

    c.setConsentPolicy(consentPolicy);
    c.setIpAddress(ip);
    c.setUserAgent(ua);
    c.setLocale(dto.getLocale());
    if (dto.getSource() != null && !dto.getSource().isBlank()) {
      c.setSource(dto.getSource());
    }
    return repo.save(c);
  }

  @Transactional
  @Override
  public ConsentDTO approve(Long consentId, User actor) {
    Consent c = repo.findById(consentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CONSENT_NOT_FOUND"));

    boolean isOwner = c.getUser().getId().equals(actor.getId());
    boolean isAdmin = actor.getRole().equals("ROLE_ADMIN");
    if (!isOwner && !isAdmin) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT_ALLOWED");
    }

    if (c.getPurpose().equals(ConsentPurpose.KVKK_NOTICE_ACK)) {
      c.setStatus(ConsentStatus.ACKNOWLEDGED);
    } else {
      c.setStatus(ConsentStatus.ACCEPTED);
    }
    syncTimestampsForStatus(c);
    c = repo.save(c);

    return mapper.entityToDTO(c);
  }

  @Transactional
  @Override
  public ConsentDTO withdraw(Long consentId, User actor) {
    Consent c = repo.findById(consentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CONSENT_NOT_FOUND"));

    boolean isOwner = c.getUser().getId().equals(actor.getId());
    boolean isAdmin = actor.getRole().equals("ROLE_ADMIN");
    if (!isOwner && !isAdmin) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT_ALLOWED");
    }

    // zaten withdrawn ise dokunma
    if (c.getStatus() != ConsentStatus.WITHDRAWN) {
      c.setStatus(ConsentStatus.WITHDRAWN);
      syncTimestampsForStatus(c); // <-- EKLE
      c = repo.save(c);
    }
    return mapper.entityToDTO(c);
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

  private static Timestamp nowTs() {
    return new Timestamp(System.currentTimeMillis());
  }

  private void syncTimestampsForStatus(Consent c) {
    switch (c.getStatus()) {
      case ACCEPTED:
      case ACKNOWLEDGED:
        if (c.getGrantedAt() == null)
          c.setGrantedAt(nowTs());
        c.setWithdrawnAt(null);
        break;
      case WITHDRAWN:
        c.setWithdrawnAt(nowTs());
        c.setGrantedAt(null); // <-- kritik: constraint için
        break;
      case REJECTED:
        c.setGrantedAt(null);
        c.setWithdrawnAt(null);
        break;
    }
  }
}