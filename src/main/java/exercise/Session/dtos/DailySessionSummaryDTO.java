package exercise.Session.dtos;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySessionSummaryDTO {
  private LocalDate day; // örn: 2025-08-23
  private long sessionsCount; // o gün kaç oturum
  private long totalActiveMs; // o gün toplam aktif süre (ms)

  public double getTotalActiveMinutes() {
    return Math.round((totalActiveMs / 60000.0) * 100.0) / 100.0;
  }
}