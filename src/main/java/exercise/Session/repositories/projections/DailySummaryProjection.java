package exercise.Session.repositories.projections;

import java.sql.Timestamp;

public interface DailySummaryProjection {
  Timestamp getDay();

  long getSessionsCount();

  long getTotalActiveMs();
}