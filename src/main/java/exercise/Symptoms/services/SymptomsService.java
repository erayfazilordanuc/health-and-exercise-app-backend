package exercise.Symptoms.services;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import exercise.Symptoms.dtos.CreateSymptomsDTO;
import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.Symptoms.dtos.UpdateSymptomsDTO;
import exercise.Symptoms.entities.Symptoms;
import exercise.Symptoms.mappers.SymptomsMapper;
import exercise.Symptoms.repositories.SymptomsRepository;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;

@Service
public class SymptomsService {

    @Autowired
    private SymptomsRepository symptomsRepo;

    @Autowired
    private SymptomsMapper symptomsMapper;

    @Autowired
    private UserRepository userRepo;

    public Symptoms createSymptoms(CreateSymptomsDTO symptomsDTO) {
        User user = userRepo.findById(symptomsDTO.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        ;

        Symptoms newSymptoms = new Symptoms(null, symptomsDTO.getPulse(),
                symptomsDTO.getSteps(),
                symptomsDTO.getSleep(), symptomsDTO.getSleepSession(), user, null, null);
        Symptoms savedSymptoms = symptomsRepo.save(newSymptoms);

        return savedSymptoms;
    }

    public Symptoms getSymptomsById(Long id) {
        return null;
    }

    public Symptoms getSymptomsByAuthorId(Long id) {
        return null;
    }

    public Symptoms updateSymptoms(Long id, UpdateSymptomsDTO symptomsDTO, Long userId) {
        Symptoms symptoms = symptomsRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Symptoms not found"));
        ;

        if (!Objects.equals(userId,
                symptoms.getUser().getId())) {
            throw new Error("This symptoms is not yours");
        }

        System.out.println(symptoms);

        symptoms.setPulse(symptomsDTO.getPulse());
        symptoms.setSteps(symptomsDTO.getSteps());
        symptoms.setSleep(symptomsDTO.getSleep());
        symptoms.setSleepSession(symptomsDTO.getSleepSession());

        Symptoms savedSymptoms = symptomsRepo.save(symptoms);

        return savedSymptoms;
    }
}
