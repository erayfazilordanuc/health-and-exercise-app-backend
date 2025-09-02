package exercise.Symptoms;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import exercise.Symptoms.dtos.StepGoalDTO;
import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.Symptoms.dtos.UpsertSymptomsDTO;
import exercise.Symptoms.entities.Symptoms;
import exercise.Symptoms.services.StepGoalService;
import exercise.Symptoms.services.SymptomsService;
import exercise.User.entities.User;

@RestController
@RequestMapping("/api/symptoms")
@Tags(value = @Tag(name = "Symptoms Operations"))
public class SymptomsController {

  @Autowired
  public SymptomsService symptomsService;

  @Autowired
  public StepGoalService stepGoalService;

  @GetMapping
  public List<Symptoms> getSymptomsByUser(
      @AuthenticationPrincipal User user) {
    List<Symptoms> symptoms = symptomsService.getAllSymptomsByUserId(user.getId(), null);
    return symptoms;
  }

  @GetMapping("/id/{id}")
  public ResponseEntity<SymptomsDTO> getSymptomsById(
      @PathVariable Long id,
      @AuthenticationPrincipal User user) {
    if (user.getRole().equals("ROLE_ADMIN")) {
      return symptomsService.getSymptomsById(id);
    }

    ResponseEntity<SymptomsDTO> response = symptomsService.getSymptomsById(id, user);
    return response;
  }

  // sonuna /all eklenebilir
  @GetMapping("/{date}")
  public List<Symptoms> getAllSymptomsByDate(
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal User user) {
    List<Symptoms> symptoms = symptomsService.getAllSymptomsByUserIdAndDate(user, date);
    return symptoms;
  }

  // sonuna /latest eklenmeli
  @GetMapping("/date/{date}")
  public Symptoms getLatestSymptomsByDate(
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal User user) {
    Symptoms symptoms = symptomsService.getLatestSymptomsByUserIdAndDate(user, date);
    return symptoms;
  }

  @PostMapping
  public Symptoms createSymptoms(
      @RequestBody UpsertSymptomsDTO symptomsDTO,
      @AuthenticationPrincipal User user) {
    Symptoms symptoms = symptomsService.createSymptoms(symptomsDTO, user);

    return symptoms;
  }

  @PutMapping("/id/{id}")
  public Symptoms upsertSymptomsById(
      @PathVariable Long id, @RequestBody UpsertSymptomsDTO symptomsDTO,
      @AuthenticationPrincipal User user) {

    Symptoms symptoms = symptomsService.upsertSymptoms(id, symptomsDTO, user);

    return symptoms;
  }

  @PutMapping("/date/{date}") // sonuna latest lazım
  public Symptoms upsertSymptomsByDate(
      @PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestBody UpsertSymptomsDTO symptomsDTO,
      @AuthenticationPrincipal User user) {
    Symptoms symptoms = symptomsService.upsertSymptoms(date, symptomsDTO, user);
    return symptoms;
  }

  @DeleteMapping("/id/{id}")
  public String deleteSymptomsById(@PathVariable Long id, @AuthenticationPrincipal User user) {
    String response = symptomsService.deleteSymptoms(id, user);
    return response;
  }

  @PutMapping("/step-goal")
  public StepGoalDTO createStepGoal(@RequestParam Integer goal, @AuthenticationPrincipal User user) {
    StepGoalDTO response = stepGoalService.create(goal, user);
    return response;
  }

  @PutMapping("/step-goal/id/{id}/done")
  public StepGoalDTO completeGoal(@PathVariable Long id, @AuthenticationPrincipal User user) {
    StepGoalDTO response = stepGoalService.complete(id, user.getId());
    return response;
  }

  @GetMapping("/steps/weekly")
  public Integer getWeeklyStepGoalProgress(@AuthenticationPrincipal User user) {
    Integer response = symptomsService.getWeeklySteps(user.getId());
    return response;
  }

  @GetMapping("/step-goal/weekly")
  public StepGoalDTO getWeeklyStepGoal(@AuthenticationPrincipal User user) {
    StepGoalDTO response = stepGoalService.getWeeklyByUserId(user.getId());
    return response;
  }

  @GetMapping("/step-goal/done")
  public List<StepGoalDTO> getDoneStepGoals(@AuthenticationPrincipal User user) {
    List<StepGoalDTO> response = stepGoalService.getDonesByUserId(user.getId());
    return response;
  }

  @Tag(name = "Admin Operations")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/user/id/{id}/steps/weekly")
  public Integer getWeeklyStepGoalProgressByUserId(@PathVariable Long id) {
    Integer response = symptomsService.getWeeklySteps(id);
    return response;
  }

  @Tag(name = "Admin Operations")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/user/id/{id}/step-goal/weekly")
  public StepGoalDTO getWeeklyStepGoalByUserId(@PathVariable Long id) {
    StepGoalDTO response = stepGoalService.getWeeklyByUserId(id);
    return response;
  }

  @Tag(name = "Admin Operations")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/user/id/{id}/step-goal/done")
  public List<StepGoalDTO> getDoneStepGoalsByUserId(@PathVariable Long id) {
    List<StepGoalDTO> response = stepGoalService.getDonesByUserId(id);
    return response;
  }

  @Tag(name = "Admin Operations")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/user/id/{id}")
  public List<Symptoms> getByUserId(@PathVariable Long id, @AuthenticationPrincipal User user) {
    List<Symptoms> symptoms = symptomsService.getAllSymptomsByUserId(id, user);
    return symptoms;
  }

  @Tag(name = "Admin Operations")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/user/id/{id}/date/{date}")
  public Symptoms getSymptomsByUserIdAndDate(
      @PathVariable Long id,
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @AuthenticationPrincipal User user) {
    Symptoms symptoms = symptomsService.getLatestSymptomsByUserIdAndDateForAdmin(id, date, user);
    return symptoms;
  }
}
