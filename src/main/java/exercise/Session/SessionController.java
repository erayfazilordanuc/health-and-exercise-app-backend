package exercise.Session;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exercise.Session.dtos.DailySessionSummaryDTO;
import exercise.Session.dtos.SessionDTO;
import exercise.Session.services.SessionService;
import exercise.User.entities.User;

@RestController
@RequestMapping("/api/sessions")
@Tags(value = @Tag(name = "Session Operations"))
@RequiredArgsConstructor
public class SessionController {

  private final SessionService service;

  @PostMapping("/heartbeat")
  public ResponseEntity<Void> heartbeat(@RequestParam UUID sessionId,
      @AuthenticationPrincipal User user) {
    service.heartbeat(sessionId, user);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/partial")
  public ResponseEntity<Void> partial(@RequestParam UUID sessionId,
      @RequestParam Long activeMs,
      @AuthenticationPrincipal User user) {
    service.partial(sessionId, activeMs, user);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/close")
  public ResponseEntity<Void> close(@RequestParam UUID sessionId,
      @RequestParam Long activeMs,
      @RequestParam(required = false) String reason, @AuthenticationPrincipal User user) {
    service.close(sessionId, activeMs, reason, user);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public ResponseEntity<List<SessionDTO>> getMySessions(
      @AuthenticationPrincipal User user,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
    return ResponseEntity.ok(service.listSessionsForUser(user.getId(), from, to, null));
  }

  @Tag(name = "Admin Operations")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/users/{userId}")
  public ResponseEntity<List<SessionDTO>> getUserSessions(
      @PathVariable Long userId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(service.listSessionsForUser(userId, from, to, user));
  }

  @Tag(name = "Admin Operations")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/users/{userId}/weekly")
  public ResponseEntity<List<DailySessionSummaryDTO>> getWeeklySummary(
      @PathVariable Long userId,
      @AuthenticationPrincipal User user,
      @RequestParam(defaultValue = "7") int days) {
    return ResponseEntity.ok(service.getDailySummary(user.getId(), days, user));
  }
}
