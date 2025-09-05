package exercise.Symptoms.services;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Istanbul"));
        Timestamp startOfDay = Timestamp.valueOf(today.atStartOfDay());
        Symptoms existingSymptoms = symptomsRepo.findLatestByUserIdAndDate(user.getId(), startOfDay);

        // son semptom ile gelen semptomu karşılaştırsın, eğer aynı ise yenisini
        // eklemesin
        Boolean isSame = true;
        if (!Objects.isNull(existingSymptoms)) {
            if (!Objects.equals(existingSymptoms.getPulse(), symptomsDTO.getPulse()))
                isSame = false;
            if (!Objects.equals(existingSymptoms.getSteps(), symptomsDTO.getSteps()))
                isSame = false;
            if (!Objects.equals(existingSymptoms.getTotalCaloriesBurned(), symptomsDTO.getTotalCaloriesBurned()))
                isSame = false;
            if (!Objects.equals(existingSymptoms.getActiveCaloriesBurned(), symptomsDTO.getActiveCaloriesBurned()))
                isSame = false;
            if (!Objects.equals(existingSymptoms.getSleepMinutes(), symptomsDTO.getSleepMinutes()))
                isSame = false;
        } else
            isSame = false;

        if (!isSame) {
            Symptoms newSymptoms = new Symptoms(null, symptomsDTO.getPulse(),
                    symptomsDTO.getSteps(), symptomsDTO.getTotalCaloriesBurned(), symptomsDTO.getActiveCaloriesBurned(),
                    symptomsDTO.getSleepMinutes(), user, null, null);
            Symptoms savedSymptoms = symptomsRepo.save(newSymptoms);

            return savedSymptoms;
        }

        return existingSymptoms;
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
        Symptoms existingSymptoms = symptomsRepo.findLatestByUserIdAndDate(user.getId(), startOfDay);
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

    public Integer getWeeklySteps(Long userId) {
        ZoneId TR = ZoneId.of("Europe/Istanbul");
        LocalDate todayTr = LocalDate.now(TR);
        LocalDate mondayTr = todayTr.with(DayOfWeek.MONDAY);

        // [monday, today+1) gün aralığı
        LocalDate startDate = mondayTr;
        LocalDate endDate = todayTr.plusDays(1); // exclusive

        return symptomsRepo.sumStepsOfLatestPerDayInRangePgByDate(userId, startDate, endDate);
    }

    public int getAverageWeeklyStepsExcludingCurrent(Long userId) {
        ZoneId TR = ZoneId.of("Europe/Istanbul");

        LocalDate todayTr = LocalDate.now(TR);
        LocalDate currentMonday = todayTr.with(DayOfWeek.MONDAY);
        LocalDateTime currentWeekStart = currentMonday.atStartOfDay();

        List<Symptoms> allBeforeThisWeek = symptomsRepo.findAllByUserIdAndCreatedAtBefore(userId, currentWeekStart);
        if (allBeforeThisWeek.isEmpty())
            return 0;

        Map<LocalDate, Long> weeklyTotals = new HashMap<>();

        for (Symptoms s : allBeforeThisWeek) {
            Integer steps = s.getSteps();
            if (steps == null || steps <= 0)
                continue;

            LocalDate createdDateTr = s.getCreatedAt()
                    .toInstant()
                    .atZone(TR)
                    .toLocalDate();

            LocalDate weekMonday = createdDateTr.with(DayOfWeek.MONDAY);
            weeklyTotals.merge(weekMonday, steps.longValue(), Long::sum);
        }

        if (weeklyTotals.isEmpty())
            return 0;

        double avg = weeklyTotals.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        return (int) Math.round(avg);
    }

    public int getThisWeekTotalSteps(Long userId) {
        ZoneId TR = ZoneId.of("Europe/Istanbul");

        LocalDate monday = LocalDate.now(TR)
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Hafta başlangıcı (Pazartesi 00:00)
        ZonedDateTime zStart = monday.atStartOfDay(TR);
        // Şu an (gelecek günleri dahil etmemek için)
        ZonedDateTime zEnd = ZonedDateTime.now(TR);

        Timestamp start = Timestamp.from(zStart.toInstant());
        Timestamp end = Timestamp.from(zEnd.toInstant());

        Long sum = symptomsRepo.sumStepsBetween(userId, start, end);
        return sum == null ? 0 : sum.intValue();
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

    public ResponseEntity<SymptomsDTO> getSymptomsById(Long id) {
        Symptoms symptoms = symptomsRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Symptoms not found"));

        SymptomsDTO dto = symptomsMapper.entityToDTO(symptoms);
        return ResponseEntity.ok(dto);
    }

    public List<Symptoms> getAllSymptomsByUserIdAndDate(User user, LocalDate date) {
        Timestamp startOfDay = Timestamp.valueOf(date.atStartOfDay());
        List<Symptoms> symptoms = symptomsRepo.findByUserIdAndDate(user.getId(), startOfDay);

        return symptoms;
    }

    public Symptoms getLatestSymptomsByUserIdAndDate(User user, LocalDate date) {
        Timestamp startOfDay = Timestamp.valueOf(date.atStartOfDay());
        Symptoms symptoms = symptomsRepo.findLatestByUserIdAndDate(user.getId(), startOfDay);

        return symptoms;
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

    // Admin Methods
    public List<Symptoms> getAllSymptomsByUserId(Long userId, User actor) {
        if (!Objects.equals(userId, actor.getId())) { // if true, the actor is admin
            if (!userService.checkUserConsentState(userId)) // !userService.checkUserConsentState(actor.getId()) ||
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "KVKK consent required");
        }

        List<Symptoms> symptoms = symptomsRepo.findByUserId(userId);

        return symptoms;
    }

    public Symptoms getLatestSymptomsByUserIdAndDateForAdmin(Long userId, LocalDate date, User actor) {
        if (!Objects.equals(userId, actor.getId())) { // actor varsa admin
            if (!userService.checkUserConsentState(userId))
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "KVKK consent required");
        }

        Timestamp startOfDay = Timestamp.valueOf(date.atStartOfDay());
        Timestamp endOfDay = Timestamp.valueOf(date.plusDays(1).atStartOfDay());

        Symptoms symptoms = symptomsRepo.findLatestByUserIdAndDate(userId, startOfDay);

        Double avgPulse = symptomsRepo.findAvgPulseByUserIdAndDate(userId, startOfDay, endOfDay);

        if (avgPulse != null) {
            symptoms.setPulse((int) Math.round(avgPulse));
        } else {
            symptoms.setPulse(null);
        }

        return symptoms;
    }
}
