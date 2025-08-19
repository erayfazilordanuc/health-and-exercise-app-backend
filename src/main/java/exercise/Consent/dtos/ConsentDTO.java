package exercise.Consent.dtos;

import java.util.Objects;

import exercise.Consent.entities.Consent;
import exercise.Consent.entities.ConsentPolicy;
import exercise.Consent.enums.ConsentPurpose;
import exercise.Consent.enums.ConsentStatus;
import exercise.User.entities.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ConsentDTO {

        private Long id;

        private ConsentPurpose purpose;

        private ConsentStatus status;

        private ConsentPolicyDTO policyDTO;

        private Long userId;

        private String locale;
        private String source;
}