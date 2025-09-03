package exercise.Session.services;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import exercise.Session.dtos.DailySessionSummaryDTO;
import exercise.Session.dtos.SessionDTO;
import exercise.Session.entities.Session;
import exercise.Session.repositories.SessionRepository;
import exercise.Session.repositories.projections.DailySummaryProjection;
import exercise.User.entities.User;
import exercise.User.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {

  private final SessionRepository repo;

  private final UserService userService;

  public void heartbeat(UUID sessionId, User user) {
    Optional<Session> opt = repo.findBySessionId(sessionId);
    Session s = opt.orElseGet(() -> {
      Session ns = new Session();
      ns.setSessionId(sessionId);
      ns.setUser(user);
      ns.setStartedAt(Timestamp.from(Instant.now()));
      return ns;
    });
    if (s.getEndedAt() == null) {
      s.setLastHeartbeatAt(Timestamp.from(Instant.now()));
      s.setHeartbeatCount(s.getHeartbeatCount() + 1);
      repo.save(s);
    }
  }

  public void partial(UUID sessionId, Long activeMs, User user) {
    Optional<Session> opt = repo.findBySessionId(sessionId);
    Session s = opt.orElseGet(() -> {
      Session ns = new Session();
      ns.setSessionId(sessionId);
      ns.setUser(user);
      ns.setStartedAt(Timestamp.from(Instant.now()));
      return ns;
    });
    if (s.getEndedAt() == null) {
      s.setActiveMs(Math.max(s.getActiveMs(), activeMs));
      s.setLastHeartbeatAt(Timestamp.from(Instant.now()));
      s.setHeartbeatCount(s.getHeartbeatCount() + 1);
      repo.save(s);
    }
  }

  public void close(UUID sessionId, Long activeMs, String reason, User user) {
    Optional<Session> opt = repo.findBySessionId(sessionId);
    Session s = opt.orElseGet(() -> {
      Session ns = new Session();
      ns.setSessionId(sessionId);
      ns.setUser(user);
      ns.setStartedAt(Timestamp.from(Instant.now()));
      return ns;
    });
    if (s.getEndedAt() == null) {
      s.setEndedAt(Timestamp.from(Instant.now()));
      s.setActiveMs(Math.max(s.getActiveMs(), activeMs));
      s.setReason(reason != null ? reason : "close");

      long durationMs = Duration.between(s.getStartedAt().toInstant(),
          s.getEndedAt().toInstant()).toMillis();
      if (durationMs < 0) {
        throw new IllegalArgumentException("endedAt cannot be before startedAt");
      }
      if (durationMs > 7 * 24 * 60 * 60 * 1000L) {
        throw new IllegalArgumentException("session duration too large");
      }
      long safeActive = Math.min(activeMs != null ? activeMs : 0L, durationMs);

      s.setActiveMs(Math.max(s.getActiveMs(), safeActive));
      repo.save(s);
    }
  }

  @Transactional
  public List<SessionDTO> listSessionsForUser(Long userId, Instant from, Instant to, User actor) {
    if (!Objects.equals(userId, actor.getId())) { // if true, the actor is admin
      if (!userService.checkUserConsentState(userId)) // !userService.checkUserConsentState(actor.getId()) ||
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "KVKK consent required");
    }

    Instant fromI = (from != null) ? from : Instant.EPOCH; // 1970-01-01
    Instant toI = (to != null) ? to : Instant.parse("9999-12-31T23:59:59.999Z");

    Timestamp fromTs = Timestamp.from(fromI);
    Timestamp toTs = Timestamp.from(toI);

    return repo.findByUserAndRange(userId, fromTs, toTs)
        .stream().map(this::toDTO).toList();

  }

  @Transactional
  public List<DailySessionSummaryDTO> getDailySummary(Long userId, int days, User actor) {
    if (!Objects.equals(userId, actor.getId())) { // if true, the actor is admin
      if (!userService.checkUserConsentState(userId)) // !userService.checkUserConsentState(actor.getId()) ||
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "KVKK consent required");
    }

    List<DailySummaryProjection> rows = repo.dailySummary(userId, Math.max(days, 1));
    return rows.stream().map(p -> DailySessionSummaryDTO.builder()
        .day(p.getDay().toInstant().atZone(ZoneOffset.UTC).toLocalDate())
        .sessionsCount(p.getSessionsCount())
        .totalActiveMs(p.getTotalActiveMs())
        .build()).toList();
  }

  private SessionDTO toDTO(Session s) {
    if (s == null)
      return null;

    return SessionDTO.builder()
        .id(s.getId())
        .userId(s.getUser() != null ? s.getUser().getId() : null)
        .sessionId(s.getSessionId())
        .startedAt(s.getStartedAt())
        .endedAt(s.getEndedAt())
        .activeMs(s.getActiveMs())
        .lastHeartbeatAt(s.getLastHeartbeatAt())
        .heartbeatCount(s.getHeartbeatCount())
        .reason(s.getReason())
        .source(s.getSource())
        .createdAt(s.getCreatedAt())
        .updatedAt(s.getUpdatedAt())
        .build();
  }
}