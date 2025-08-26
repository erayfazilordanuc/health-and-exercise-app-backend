package exercise.Exercise.dtos;

import java.util.List;

public record ExerciseScheduleRequest(List<Long> activeDays) {
}