package exercise.Symptoms;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import exercise.Symptoms.dtos.CreateSymptomsDTO;
import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.Symptoms.dtos.UpdateSymptomsDTO;
import exercise.Symptoms.entities.Symptoms;
import exercise.Symptoms.services.SymptomsService;
import exercise.User.entities.User;

@RestController
@RequestMapping("api/symptoms")
@Tags(value = @Tag(name = "Symptoms Operations"))
public class SymptomsController {

  @Autowired
  public SymptomsService symptomsService;

  // @Tag(name = "Symptoms - GET Operations")
  @GetMapping("/{id}")
  public ResponseEntity<SymptomsDTO> getSymptomsById(@PathVariable Long id,
      @AuthenticationPrincipal User user) {
    ResponseEntity<SymptomsDTO> response = symptomsService.getSymptomsById(id, user);
    return response;
  }

  // @Tag(name = "Symptoms - GET Operations")
  @GetMapping("/user")
  public List<Symptoms> getSymptomsByUserId(@AuthenticationPrincipal User user) {
    List<Symptoms> symptoms = symptomsService.getSymptomsById(user); // TO DO THINK Burda da ResponseEntity dönmeli mi
    return symptoms;
  }

  // @Tag(name = "Symptoms - POST Operations")
  @PostMapping
  public Symptoms createSymptoms(@RequestBody CreateSymptomsDTO symptomsDTO,
      @AuthenticationPrincipal User user) {

    Symptoms symptoms = symptomsService.createSymptoms(symptomsDTO, user);

    return symptoms;
  }

  @PutMapping("/{id}")
  public Symptoms upsertSymptomsById(@PathVariable Long id, @RequestBody UpdateSymptomsDTO symptomsDTO,
      @AuthenticationPrincipal User user) {

    Symptoms symptoms = symptomsService.upsertSymptoms(id, symptomsDTO, user);

    return symptoms;
  }

  @PutMapping("/date")
  public Symptoms upsertSymptomsByDate(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestBody UpdateSymptomsDTO symptomsDTO,
      @AuthenticationPrincipal User user) {
    Symptoms symptoms = symptomsService.upsertSymptoms(date, symptomsDTO, user);
    return symptoms;
  }

  @DeleteMapping("/{id}")
  public String deleteSymptomsById(@PathVariable Long id, @AuthenticationPrincipal User user) {
    String response = symptomsService.deleteSymptoms(id, user);
    return response;
  }

  @DeleteMapping
  public String deleteSymptomsByIds(@RequestBody List<Long> ids, @AuthenticationPrincipal User user) {
    String response = symptomsService.deleteSymptoms(ids, user);
    return response;
  }
}
