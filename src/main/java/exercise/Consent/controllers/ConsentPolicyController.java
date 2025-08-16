package exercise.Consent.controllers;

import lombok.RequiredArgsConstructor;

import java.time.Duration;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exercise.Consent.dtos.PolicyResponse;
import exercise.Consent.enums.ConsentPolicyPurpose;
import exercise.Consent.services.ConsentPolicyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

@RestController
@RequestMapping("/api/consent-policies")
@Tags(value = @Tag(name = "Consent Policy Operations"))
@RequiredArgsConstructor
public class ConsentPolicyController {
  private final ConsentPolicyService service;

  @GetMapping("/latest")
  public ResponseEntity<PolicyResponse> latest(
      @RequestParam ConsentPolicyPurpose purpose,
      @RequestParam(defaultValue = "tr-TR") String locale,
      @RequestParam(defaultValue = "false") boolean includeContent,
      @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

    var p = service.latest(purpose, locale);
    String etag = "\"" + p.getContentHash() + "\"";
    if (etag.equals(ifNoneMatch)) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
    }
    return ResponseEntity.ok()
        .eTag(etag)
        .cacheControl(CacheControl.maxAge(Duration.ofHours(12)).cachePublic())
        .body(PolicyResponse.of(p, includeContent));
  }

  @GetMapping("/{version}")
  public PolicyResponse byVersion(
      @PathVariable String version,
      @RequestParam ConsentPolicyPurpose purpose,
      @RequestParam(defaultValue = "tr-TR") String locale,
      @RequestParam(defaultValue = "true") boolean includeContent) {
    return PolicyResponse.of(service.byVersion(purpose, version, locale), includeContent);
  }
}
