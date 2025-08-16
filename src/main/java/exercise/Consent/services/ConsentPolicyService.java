package exercise.Consent.services;

import exercise.Consent.entities.ConsentPolicy;
import exercise.Consent.enums.ConsentPolicyPurpose;

public interface ConsentPolicyService {
  ConsentPolicy latest(ConsentPolicyPurpose purpose, String locale);

  ConsentPolicy byVersion(ConsentPolicyPurpose purpose, String version, String locale);
}
