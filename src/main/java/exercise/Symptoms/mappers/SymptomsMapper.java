package exercise.Symptoms.mappers;

import org.springframework.stereotype.Component;

import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.Symptoms.entities.Symptoms;
import exercise.Symptoms.mappers.SymptomsMapper;

@Component
public class SymptomsMapper {

    public SymptomsDTO entityToDTO(Symptoms symptoms) {
        SymptomsDTO symptomsDTO = new SymptomsDTO(symptoms.getId(), symptoms.getPulse(),
                symptoms.getSteps(), symptoms.getActiveCaloriesBurned(),
                symptoms.getSleepHours(),
                symptoms.getSleepSessions(),
                symptoms.getUser().getId(), symptoms.getCreatedAt(), symptoms.getUpdatedAt());

        return symptomsDTO;
    }
}
