package exercise.Consent.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import exercise.Consent.entities.ConsentPolicy;
import exercise.Consent.enums.ConsentPolicyPurpose;
import exercise.Consent.repositories.ConsentPolicyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsentPolicyServiceImpl implements ConsentPolicyService {

  private final ConsentPolicyRepository repo;

  private static final String DEFAULT_LOCALE = "tr-TR";
  private static final List<String> GLOBAL_FALLBACKS = List.of("en-GB", "en-US", "en");

  @Override
  public ConsentPolicy latest(ConsentPolicyPurpose purpose, String locale) {
    for (String cand : candidateLocales(locale)) {
      var hit = repo.findTopByPurposeAndLocaleOrderByEffectiveAtDesc(purpose, cand);
      if (hit.isPresent())
        return hit.get();
    }
    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
        "No policy found for purpose=%s locale=%s".formatted(purpose, locale));
  }

  @Override
  public ConsentPolicy byVersion(ConsentPolicyPurpose purpose, String version, String locale) {
    for (String cand : candidateLocales(locale)) {
      var hit = repo.findByPurposeAndVersionAndLocale(purpose, version, cand);
      if (hit.isPresent())
        return hit.get();
    }
    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
        "Policy not found for purpose=%s version=%s locale=%s".formatted(purpose, version, locale));
  }

  /** Örn. "tr-TR" → ["tr-TR","tr","tr-TR(default)","en-GB","en-US","en"] */
  private List<String> candidateLocales(String locale) {
    if (locale == null || locale.isBlank())
      locale = DEFAULT_LOCALE;
    String norm = locale.replace('_', '-');

    List<String> cands = new ArrayList<>();
    cands.add(norm);
    int dash = norm.indexOf('-');
    if (dash > 0)
      cands.add(norm.substring(0, dash)); // "tr"

    // explicit default
    if (!cands.contains(DEFAULT_LOCALE))
      cands.add(DEFAULT_LOCALE);
    String baseDefault = DEFAULT_LOCALE.substring(0, DEFAULT_LOCALE.indexOf('-'));
    if (!cands.contains(baseDefault))
      cands.add(baseDefault);

    // global fallbacks
    for (String f : GLOBAL_FALLBACKS) {
      if (!cands.contains(f))
        cands.add(f);
    }
    return cands;
  }
}