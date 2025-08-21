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

import exercise.Symptoms.dtos.UpsertSymptomsDTO;
import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.Symptoms.dtos.UpsertSymptomsDTO;
import exercise.Symptoms.entities.Symptoms;
import exercise.Symptoms.mappers.SymptomsMapper;
import exercise.Symptoms.repositories.SymptomsRepository;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;
import exercise.User.services.UserService;

@Service
public class SymptomsService {

    @Autowired
    private SymptomsRepository symptomsRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private SymptomsMapper symptomsMapper;

    public Symptoms createSymptoms(UpsertSymptomsDTO symptomsDTO, User user) {
        Symptoms newSymptoms = new Symptoms(null, symptomsDTO.getPulse(),
                symptomsDTO.getSteps(), symptomsDTO.getTotalCaloriesBurned(), symptomsDTO.getActiveCaloriesBurned(),
                symptomsDTO.getSleepMinutes(), user, null, null);
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

    public List<Symptoms> getAllSymptomsByUserId(Long userId, User actor) {
        if (userId != actor.getId()
                && !userService.checkUserConsentState(userId) || !userService.checkUserConsentState(actor.getId()))
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "KVKK consent required");

        List<Symptoms> symptoms = symptomsRepo.findByUserId(userId);

        return symptoms;
    }

    public ResponseEntity<SymptomsDTO> getSymptomsById(Long id) {
        Symptoms symptoms = symptomsRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Symptoms not found"));

        SymptomsDTO dto = symptomsMapper.entityToDTO(symptoms);
        return ResponseEntity.ok(dto);
    }

    public Symptoms getSymptomsByUserIdAndDate(User user, LocalDate date) {
        Timestamp startOfDay = Timestamp.valueOf(date.atStartOfDay());
        Symptoms symptoms = symptomsRepo.findByUserIdAndDate(user.getId(), startOfDay);

        return symptoms;
    }

    public Symptoms getSymptomsByUserIdAndDate(Long userId, LocalDate date, User actor) {
        if (userId != actor.getId()
                && !userService.checkUserConsentState(userId) || !userService.checkUserConsentState(actor.getId()))
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "KVKK consent required");

        Timestamp startOfDay = Timestamp.valueOf(date.atStartOfDay());
        Symptoms symptoms = symptomsRepo.findByUserIdAndDate(userId, startOfDay);

        return symptoms;
    }

    public Symptoms upsertSymptoms(Long id, UpsertSymptomsDTO symptomsDTO, User user) {
        Optional<Symptoms> optionalSymptoms = symptomsRepo.findById(id);

        Symptoms symptoms = optionalSymptoms.orElseGet(() -> {
            Symptoms s = new Symptoms();
            s.setUser(user);
            return s;
        });

        if (optionalSymptoms.isPresent()) {
            if (!Objects.equals(user.getId(), symptoms.getUser().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This symptoms is not yours");
            }
        }

        System.out.println(symptoms);

        symptoms.setPulse(symptomsDTO.getPulse());
        symptoms.setSteps(symptomsDTO.getSteps());
        symptoms.setTotalCaloriesBurned(symptomsDTO.getTotalCaloriesBurned());
        symptoms.setActiveCaloriesBurned(symptomsDTO.getActiveCaloriesBurned());
        symptoms.setSleepMinutes(symptomsDTO.getSleepMinutes());

        Symptoms savedSymptoms = symptomsRepo.save(symptoms);

        return savedSymptoms;
    }

    public Symptoms upsertSymptoms(LocalDate date, UpsertSymptomsDTO symptomsDTO, User user) {
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
        symptoms.setTotalCaloriesBurned(symptomsDTO.getTotalCaloriesBurned());
        symptoms.setActiveCaloriesBurned(symptomsDTO.getActiveCaloriesBurned());
        symptoms.setSleepMinutes(symptomsDTO.getSleepMinutes());

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
