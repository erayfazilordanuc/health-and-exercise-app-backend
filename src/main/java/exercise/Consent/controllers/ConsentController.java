package exercise.Consent.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exercise.Consent.dtos.ConsentDTO;
import exercise.Consent.dtos.UpsertConsentDTO;
import exercise.Consent.entities.Consent;
import exercise.Consent.enums.ConsentPurpose;
import exercise.Consent.services.ConsentService;
import exercise.User.entities.User;

@RestController
@RequestMapping("/api/consents")
@Tags(value = @Tag(name = "Consent Operations"))
@RequiredArgsConstructor
public class ConsentController {

  private final ConsentService service;

  @PostMapping
  public ResponseEntity<Consent> give(@Valid @RequestBody UpsertConsentDTO dto,
      HttpServletRequest req,
      @AuthenticationPrincipal User user) {
    Long userId = user.getId();
    String ip = clientIp(req);
    String ua = req.getHeader("User-Agent");
    Consent saved = service.upsertConsent(userId, dto, ip, ua);

    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @PutMapping("/{id}/approve")
  public ConsentDTO approve(@PathVariable Long id,
      @AuthenticationPrincipal User user) {
    // Not: owner veya admin kontrolünü service içinde yapıyoruz
    return service.approve(id, user);
  }

  @PatchMapping("/{id}/withdraw")
  public ConsentDTO withdraw(@PathVariable Long id,
      @AuthenticationPrincipal User user) {
    // Not: owner veya admin kontrolünü service içinde yapıyoruz
    return service.withdraw(id, user);
  }

  @GetMapping("/mine/latest")
  public ConsentDTO myLatest(@RequestParam ConsentPurpose purpose,
      @AuthenticationPrincipal User user) {
    return service.latest(user.getId(), purpose);
  }

  // küçük yardımcı (proxy arkasında doğru IP için)
  private String clientIp(HttpServletRequest req) {
    String fwd = req.getHeader("X-Forwarded-For");
    if (fwd != null && !fwd.isBlank())
      return fwd.split(",")[0].trim();
    return req.getRemoteAddr();
  }
}