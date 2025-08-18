package exercise.Consent.dtos;

import exercise.Consent.entities.ConsentPolicy;
import exercise.Consent.enums.ConsentPurpose;
import exercise.Consent.enums.ConsentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConsentDTO(
        @NotNull ConsentPurpose purpose,
        @NotNull ConsentStatus status, // ACCEPTED / REJECTED / WITHDRAWN
        @NotNull Long consentPolicyId,
        String locale,
        String source // opsiyonel; null ise "MOBILE" kalır
) {
}