package exercise.Symptoms;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

@RestController
@RequestMapping("api/symptoms")
@Tags(value = @Tag(name = "Symptoms Operations"))
public class SymptomsController {

  @Autowired
  public SymptomsService symptomsService;

  @Autowired
  private SymptomsMapper symptomsMapper;

  @Autowired
  public SymptomsRepository symptomsRepo;

  @PostMapping
  public Symptoms createSymptoms(@RequestBody SymptomsDTO symptomsDTO, @AuthenticationPrincipal User user) {
    if (!Objects.equals(user.getId(),
        symptomsDTO.getOwnerId())) {
      throw new Error("You can't create a symptoms for someone else");
    }

    Symptoms symptoms = symptomsService.createSymptoms(symptomsDTO);

    return symptoms;
  }

  @GetMapping("/{id}")
  public ResponseEntity<SymptomsDTO> getSymptomsById(@PathVariable Long id, @AuthenticationPrincipal User user) {
    Symptoms symptoms = symptomsRepo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Symptoms not found"));

    if (!Objects.equals(symptoms.getOwner().getId(), user.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this data");
    }

    SymptomsDTO dto = symptomsMapper.entityToDTO(symptoms); // DTO kullanımı önerilir
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/owner/{ownerId}")
  public Symptoms getSymptomsByOwnerId(@PathVariable Long ownerId, @AuthenticationPrincipal User user) {
    if (!Objects.equals(user.getId(),
        ownerId)) {
      throw new Error("You can't see symptomss for someone else");
    }

    Symptoms symptoms = symptomsRepo.findByOwnerId(ownerId);

    return symptoms;
  }

  @PostMapping("/{id}")
  public Symptoms updateSymptoms(@PathVariable Long id, @RequestBody SymptomsDTO symptomsDTO,
      @AuthenticationPrincipal User user) {
    if (!Objects.equals(user.getId(),
        symptomsDTO.getOwnerId())) {
      throw new Error("This symptoms is not yours");
    }

    Symptoms symptoms = symptomsService.updateSymptoms(id, symptomsDTO, user.getId());

    return symptoms;
  }

  @DeleteMapping("/{id}")
  public String deleteSymptomsById(@PathVariable Long id, @AuthenticationPrincipal User user) {
    Symptoms symptoms = symptomsRepo.findById(id).get();

    if (!Objects.equals(id, symptoms.getId())) {
      throw new Error("This not is not yours");
    }

    symptomsRepo.delete(symptoms);

    return "Symptoms with id " + symptoms.getId() + " deleted";
  }
}
