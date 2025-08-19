package exercise.Consent.dtos;

import exercise.Consent.entities.Consent;
import exercise.Consent.entities.ConsentPolicy;
import exercise.Consent.enums.ConsentPurpose;
import exercise.Consent.enums.ConsentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpsertConsentDTO {
        @NotNull
        private ConsentPurpose purpose;

        @NotNull
        private ConsentStatus status; // ACCEPTED / REJECTED / WITHDRAWN

        @NotNull
        private Long policyId;

        private String locale;
        private String source; //

        public UpsertConsentDTO(Consent c) {
                this.purpose = c.getPurpose();
                this.status = c.getStatus();
                this.policyId = c.getConsentPolicy().getId();
                this.locale = c.getLocale();
                this.source = c.getSource();
        }
}