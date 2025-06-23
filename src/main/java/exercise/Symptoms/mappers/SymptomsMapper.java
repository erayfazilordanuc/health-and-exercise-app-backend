package exercise.Symptoms.mappers;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.Symptoms.entities.Symptoms;
import exercise.Symptoms.mappers.SymptomsMapper;
import exercise.Symptoms.repositories.SymptomsRepository;
import exercise.Symptoms.services.SymptomsService;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;

@Component
public class SymptomsMapper {

    @Autowired
    private UserRepository userRepo;

    // public Symptoms DTOToEntity(SymptomsDTO noteDTO, Symptoms note) {
    // Symptoms noteEntity = new Symptoms(noteDTO.)

    // return noteEntity;
    // }

    public SymptomsDTO entityToDTO(Symptoms symptoms) {
        SymptomsDTO symptomsDTO = new SymptomsDTO(symptoms.getId(), symptoms.getPulse(), symptoms.getSteps(),
                symptoms.getSleep(),
                symptoms.getSleepSession(),
                symptoms.getOwner().getId());

        return symptomsDTO;
    }
}
