package exercise.Symptoms.services;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public Symptoms createSymptoms(CreateSymptomsDTO symptomsDTO, User user) {
        Symptoms newSymptoms = new Symptoms(null, symptomsDTO.getPulse(),
                symptomsDTO.getSteps(),
                symptomsDTO.getSleep(), symptomsDTO.getSleepSession(), user, null, null);
        Symptoms savedSymptoms = symptomsRepo.save(newSymptoms);

        return savedSymptoms;
    }

    public ResponseEntity<SymptomsDTO> getSymptomsById(Long id, User user) {
        Symptoms symptoms = symptomsRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Symptoms not found"));

        if (!Objects.equals(symptoms.getUser().getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this data");
        }

        SymptomsDTO dto = symptomsMapper.entityToDTO(symptoms);
        return ResponseEntity.ok(dto);
    }

    public List<Symptoms> getAllSymptomsByUserId(User user) {
        List<Symptoms> symptoms = symptomsRepo.findByUserId(user.getId());

        return symptoms;
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

    public Symptoms upsertSymptoms(Long id, UpdateSymptomsDTO symptomsDTO, User user) {
        Optional<Symptoms> optionalSymptoms = symptomsRepo.findById(id);

        Symptoms symptoms = optionalSymptoms.orElseGet(() -> {
            Symptoms s = new Symptoms();
            s.setUser(user);
            return s;
        });

        if (optionalSymptoms.isPresent()) {
            if (!Objects.equals(user.getId(), symptoms.getUser().getId())) {
                throw new Error("This symptoms is not yours");
            }
        }

        System.out.println(symptoms);

        symptoms.setPulse(symptomsDTO.getPulse());
        symptoms.setSteps(symptomsDTO.getSteps());
        symptoms.setSleep(symptomsDTO.getSleep());
        symptoms.setSleepSession(symptomsDTO.getSleepSession());

        Symptoms savedSymptoms = symptomsRepo.save(symptoms);

        return savedSymptoms;
    }

    public Symptoms upsertSymptoms(LocalDate date, UpdateSymptomsDTO symptomsDTO, User user) {
        Symptoms symptoms = new Symptoms(user);

        Timestamp startOfDay = Timestamp.valueOf(date.atStartOfDay());
        Symptoms existingSymptoms = symptomsRepo.findByUserIdAndDate(user.getId(), startOfDay);
        if (Objects.nonNull(existingSymptoms)) {
            if (!Objects.equals(user.getId(), existingSymptoms.getUser().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This symptoms is not yours");
            }
            if (existingSymptoms.getUpdatedAt().toLocalDateTime().toLocalDate()
                    .isEqual(LocalDate.now())) {
                symptoms = existingSymptoms;
            }
        }

        System.out.println(symptoms);

        symptoms.setPulse(symptomsDTO.getPulse());
        symptoms.setSteps(symptomsDTO.getSteps());
        symptoms.setSleep(symptomsDTO.getSleep());
        symptoms.setSleepSession(symptomsDTO.getSleepSession());

        Symptoms savedSymptoms = symptomsRepo.save(symptoms);

        return savedSymptoms;
    }

    public String deleteSymptoms(Long id, User user) {
        Symptoms symptoms = symptomsRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Symptoms not found"));

        if (!Objects.equals(symptoms.getUser().getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This symptoms is not yours");
        }

        symptomsRepo.delete(symptoms);

        return "Symptoms with id " + symptoms.getId() + " deleted";
    }

    public String deleteSymptoms(List<Long> ids, User user) {
        ids.stream()
                .map(symptomsRepo::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(symptoms -> {
                    if (!Objects.equals(symptoms.getUser().getId(), user.getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This symptoms is not yours");
                    }
                })
                .forEach(symptomsRepo::delete);

        return "Symptoms deleted successfully";
    }
}
