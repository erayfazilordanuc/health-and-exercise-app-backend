package exercise.Consent.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import exercise.Consent.entities.Consent;
import exercise.Consent.entities.ConsentPolicy;
import exercise.Consent.enums.ConsentPolicyPurpose;
import exercise.Consent.enums.ConsentPurpose;

public interface ConsentPolicyRepository extends JpaRepository<ConsentPolicy, Long> {
  Optional<ConsentPolicy> findTopByPurposeAndLocaleOrderByEffectiveAtDesc(
      ConsentPolicyPurpose purpose, String locale);

  Optional<ConsentPolicy> findByPurposeAndVersionAndLocale(
      ConsentPolicyPurpose purpose, String version, String locale);
}