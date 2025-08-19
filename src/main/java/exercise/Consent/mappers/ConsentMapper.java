package exercise.Consent.mappers;

import org.springframework.stereotype.Component;

import exercise.Consent.dtos.ConsentDTO;
import exercise.Consent.dtos.ConsentPolicyDTO;
import exercise.Consent.entities.Consent;
import exercise.Consent.mappers.ConsentMapper;

@Component
public class ConsentMapper {
    public ConsentDTO entityToDTO(Consent consent) {
        ConsentDTO ConsentDTO = new ConsentDTO(consent.getId(), consent.getPurpose(),
                consent.getStatus(),
                new ConsentPolicyDTO(consent
                        .getConsentPolicy(), false),
                consent.getUser().getId(),
                consent.getLocale(),
                consent.getSource());

        return ConsentDTO;
    }
}
