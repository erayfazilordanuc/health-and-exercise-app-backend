package exercise.Symptoms.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeeklySymptomsSummary {
    private Integer avgPulse;
    private Integer maxPulse;
    private Integer minPulse;
    private Integer steps;
    private Integer totalCaloriesBurned;
    private Integer activeCaloriesBurned;
    private Integer sleepMinutes;
}
