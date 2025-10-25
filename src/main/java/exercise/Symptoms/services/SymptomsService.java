package exercise.Symptoms.services;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.Symptoms.dtos.UpsertSymptomsDTO;
import exercise.Symptoms.dtos.WeeklySymptomsSummary;
import exercise.Symptoms.entities.Symptoms;
import exercise.Symptoms.mappers.SymptomsMapper;
import exercise.Symptoms.repositories.SymptomsRepository;
import exercise.User.entities.User;
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
        if (Objects.nonNull(existingSymptoms)) {
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

            if (Objects.nonNull(existingSymptoms)) {
                if (existingSymptoms.getSteps() > symptomsDTO.getSteps()) {
                    newSymptoms.setSteps(existingSymptoms.getSteps());
                }
                if (existingSymptoms.getTotalCaloriesBurned() > symptomsDTO.getTotalCaloriesBurned()) {
                    newSymptoms.setTotalCaloriesBurned(existingSymptoms.getTotalCaloriesBurned());
                }
                if (existingSymptoms.getActiveCaloriesBurned() > symptomsDTO.getActiveCaloriesBurned()) {
                    newSymptoms.setActiveCaloriesBurned(existingSymptoms.getActiveCaloriesBurned());
                }
            }

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

    // public Integer getWeeklySteps(Long userId) {
    // ZoneId TR = ZoneId.of("Europe/Istanbul");
    // LocalDate todayTr = LocalDate.now(TR);
    // LocalDate mondayTr = todayTr.with(DayOfWeek.MONDAY);

    // // [monday, today+1) gün aralığı
    // LocalDate startDate = mondayTr;
    // LocalDate endDate = todayTr.plusDays(1); // exclusive

    // return symptomsRepo.sumStepsOfLatestPerDayInRangePgByDate(userId, startDate,
    // endDate);
    // }

    public Integer getWeeklySteps(Long userId) {
        ZoneId TR = ZoneId.of("Europe/Istanbul");

        // Haftanın başı (Pzt 00:00 TR) ve "bugün+1" 00:00 TR (exclusive)
        LocalDate todayTr = LocalDate.now(TR);
        LocalDate mondayTr = todayTr.with(DayOfWeek.MONDAY);

        ZonedDateTime zStart = mondayTr.atStartOfDay(TR);
        ZonedDateTime zEnd = todayTr.plusDays(1).atStartOfDay(TR);

        Timestamp startTs = Timestamp.from(zStart.toInstant());
        Timestamp endTs = Timestamp.from(zEnd.toInstant());

        // Gün başına "son kayıt"ları çek
        List<Symptoms> latestPerDay = symptomsRepo.findLatestPerDayInRangePg(userId, startTs, endTs);

        // Adımların toplamını al (NULL'ları 0 say)
        int total = latestPerDay.stream()
                .map(Symptoms::getSteps)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        return total;
    }

    public int getAverageWeeklyStepsExcludingCurrent(Long userId) {
        ZoneId TR = ZoneId.of("Europe/Istanbul");
        LocalDate currentMonday = LocalDate.now(TR).with(DayOfWeek.MONDAY);

        Double avg = symptomsRepo.avgWeeklyStepsBeforeMondayLatestPerDay(userId, currentMonday);
        if (avg == null)
            return 0;
        return (int) Math.round(avg);
    }

    public int getThisWeekTotalSteps(Long userId) {
        ZoneId TR = ZoneId.of("Europe/Istanbul");
        LocalDate todayTr = LocalDate.now(TR);
        LocalDate mondayTr = todayTr.with(DayOfWeek.MONDAY);

        // [monday, today+1) — gün sonu taşmaları için exclusive end
        Integer sum = symptomsRepo.sumStepsOfLatestPerDayInRangePgByDate(
                userId,
                mondayTr,
                todayTr.plusDays(1) // exclusive
        );
        return sum == null ? 0 : sum;
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

    public WeeklySymptomsSummary getSymptomsByUserIdAndDateRangeForAdmin(Long userId, LocalDate startDate,
            LocalDate endDate,
            User actor) {
        if (!Objects.equals(userId, actor.getId())) { // actor varsa admin
            if (!userService.checkUserConsentState(userId))
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "KVKK consent required");
        }

        List<Symptoms> symptoms = symptomsRepo.findLatestForEachDayInRange(userId, startDate, endDate);

        WeeklySymptomsSummary summary = new WeeklySymptomsSummary(null, null, null, null, null, null, null);

        return summary;
    }
}
