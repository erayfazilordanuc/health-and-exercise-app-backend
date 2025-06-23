package exercise.Symptoms.services;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exercise.Symptoms.dtos.SymptomsDTO;
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

    public Symptoms createSymptoms(SymptomsDTO symptomsDTO) {
        User owner = userRepo.findById(symptomsDTO.getOwnerId()).get();

        Symptoms newSymptoms = new Symptoms(null, symptomsDTO.getPulse(), symptomsDTO.getSteps(),
                symptomsDTO.getSleep(), symptomsDTO.getSleepSession(), owner, null);
        Symptoms savedSymptoms = symptomsRepo.save(newSymptoms);

        return savedSymptoms;
    }

    public Symptoms getSymptomsById(Long id) {
        return null;
    }

    public Symptoms getSymptomsByAuthorId(Long id) {
        return null;
    }

    public Symptoms updateSymptoms(Long id, SymptomsDTO symptomsDTO, Long ownerId) {
        Symptoms symptoms = symptomsRepo.findById(id).get();

        System.out.println(symptoms);

        if (!Objects.equals(symptoms.getOwner().getId(), ownerId)) {
            throw new Error("There is no Symptoms with this Symptoms id and the owner id");
        }

        Symptoms savedSymptoms = symptomsRepo.save(symptoms);

        return savedSymptoms;
    }
}
