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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import exercise.Symptoms.dtos.CreateSymptomsDTO;
import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.Symptoms.dtos.UpdateSymptomsDTO;
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

  // @Tag(name = "Symptoms - POST Operations")
  @PostMapping
  public Symptoms createSymptoms(@RequestBody CreateSymptomsDTO symptomsDTO,
      @AuthenticationPrincipal User user) {
    if (!Objects.equals(user.getId(),
        symptomsDTO.getUserId())) {
      throw new Error("You can't create a symptoms for someone else");
    }

    Symptoms symptoms = symptomsService.createSymptoms(symptomsDTO);

    return symptoms;
  }

  // @Tag(name = "Symptoms - GET Operations")
  @GetMapping("/{id}")
  public ResponseEntity<SymptomsDTO> getSymptomsById(@PathVariable Long id,
      @AuthenticationPrincipal User user) {
    Symptoms symptoms = symptomsRepo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Symptoms not found"));

    if (!Objects.equals(symptoms.getUser().getId(), user.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this data");
    }

    SymptomsDTO dto = symptomsMapper.entityToDTO(symptoms); // DTO kullanımı önerilir
    return ResponseEntity.ok(dto);
  }

  // @Tag(name = "Symptoms - GET Operations")
  @GetMapping("/user/{userId}")
  public List<Symptoms> getSymptomsByUserId(@PathVariable Long userId, @AuthenticationPrincipal User user) {
    if (!Objects.equals(user.getId(),
        userId)) {
      throw new Error("You can't see symptoms for someone else");
    }

    List<Symptoms> symptoms = symptomsRepo.findByUserId(userId);

    return symptoms;
  }

  // @PutMapping("/{id}")
  // public Symptoms updateSymptoms(@PathVariable Long id, @RequestBody UpdateSymptomsDTO symptomsDTO,
  //     @AuthenticationPrincipal User user) {

  //   Symptoms symptoms = symptomsService.updateSymptoms(id, symptomsDTO, user.getId());

  //   return symptoms;
  // }

  @PutMapping("/{id}")
  public Symptoms upsertSymptoms(@PathVariable Long id, @RequestBody UpdateSymptomsDTO symptomsDTO,
      @AuthenticationPrincipal User user) {

    Symptoms symptoms = symptomsService.upsertSymptoms(id, symptomsDTO, user);

    return symptoms;
  }

  @DeleteMapping("/{id}")
  public String deleteSymptomsById(@PathVariable Long id, @AuthenticationPrincipal User user) {
    Symptoms symptoms = symptomsRepo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Symptoms not found"));
    ;

    if (!Objects.equals(id, symptoms.getId())) {
      throw new Error("This not is not yours");
    }

    symptomsRepo.delete(symptoms);

    return "Symptoms with id " + symptoms.getId() + " deleted";
  }

  @DeleteMapping
  public String deleteSymptomsByIds(@RequestBody List<Long> ids, @AuthenticationPrincipal User user) {
    ids.stream()
        .map(symptomsRepo::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .peek(symptoms -> {
          if (!Objects.equals(symptoms.getUser().getId(), user.getId())) {
            throw new RuntimeException("This symptoms is not yours");
          }
        })
        .forEach(symptomsRepo::delete);

    return "Symptomss deleted successfully";
  }
}
