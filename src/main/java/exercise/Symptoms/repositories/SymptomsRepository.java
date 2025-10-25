package exercise.Symptoms.repositories;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import exercise.Symptoms.entities.Symptoms;

@Repository
public interface SymptomsRepository extends JpaRepository<Symptoms, Long> {

  @Query("SELECT symptoms FROM Symptoms symptoms WHERE symptoms.user.id = :userId ORDER BY symptoms.updatedAt DESC")
  public List<Symptoms> findByUserId(Long userId);

  @Query("SELECT s FROM Symptoms s WHERE DATE(s.updatedAt) = DATE(:date)")
  Symptoms findByDate(@Param("date") Timestamp date);

  @Query("SELECT s FROM Symptoms s WHERE s.user.id = :userId AND DATE(s.updatedAt) = DATE(:date) ORDER BY s.updatedAt DESC")
  List<Symptoms> findByUserIdAndDate(Long userId, Timestamp date);

  @Query(value = """
        SELECT * FROM symptoms s
        WHERE s.user_id = :userId
          AND s.updated_at::date = CAST(:targetDate AS date)
        ORDER BY s.updated_at DESC
        LIMIT 1
      """, nativeQuery = true)
  Symptoms findLatestByUserIdAndDate(Long userId, Timestamp targetDate);

  @Query(value = """
      SELECT DISTINCT ON (s.updated_at::date)
             s.*
      FROM symptoms s
      WHERE s.user_id = :userId
        AND s.updated_at::date BETWEEN CAST(:startDate AS date) AND CAST(:endDate AS date)
      ORDER BY
        s.updated_at::date,  -- Önce güne göre sırala (DISTINCT ON için zorunlu)
        s.updated_at DESC    -- Sonra o gün içindeki en yeniyi bulmak için saate göre ters sırala
      """, nativeQuery = true)
  List<Symptoms> findLatestForEachDayInRange(
      Long userId,
      LocalDate startDate,
      LocalDate endDate);

  Symptoms findByUserIdAndUpdatedAtBetweenOrderByUpdatedAtDesc(
      Long userId, Timestamp start, Timestamp end);

  Symptoms findFirstByUserIdAndUpdatedAtBetweenOrderByUpdatedAtDesc(
      Long userId, Timestamp start, Timestamp end);

  @Query("""
        SELECT AVG(s.pulse)
        FROM Symptoms s
        WHERE s.user.id = :userId
          AND s.updatedAt >= :start
          AND s.updatedAt <  :end
      """)
  Double findAvgPulseInRange(
      @Param("userId") Long userId,
      @Param("start") Timestamp start,
      @Param("end") Timestamp end);

  @Query("""
          SELECT AVG(s.pulse)
          FROM Symptoms s
          WHERE s.user.id = :userId
            AND s.updatedAt >= :start
            AND s.updatedAt <  :end
            AND s.pulse IS NOT NULL
      """)
  Double findAvgPulseByUserIdAndDate(
      @Param("userId") Long userId,
      @Param("start") Timestamp start,
      @Param("end") Timestamp end);

  @Query(value = """
      WITH ranked AS (
        SELECT s.*,
               ROW_NUMBER() OVER (
                 PARTITION BY (s.updated_at AT TIME ZONE 'Europe/Istanbul')::date
                 ORDER BY s.updated_at DESC
               ) AS rn
        FROM symptoms s
        WHERE s.user_id = :userId
          AND s.updated_at >= :startTs
          AND s.updated_at <  :endTs
      )
      SELECT * FROM ranked
      WHERE rn = 1
      ORDER BY updated_at DESC
      """, nativeQuery = true)
  List<Symptoms> findLatestPerDayInRangePg(
      @Param("userId") Long userId,
      @Param("startTs") Timestamp startTs,
      @Param("endTs") Timestamp endTs);

  @Query(value = """
      WITH ranked AS (
        SELECT
          COALESCE(s.steps, 0) AS steps,
          ROW_NUMBER() OVER (
            PARTITION BY (s.updated_at AT TIME ZONE 'Europe/Istanbul')::date
            ORDER BY s.updated_at DESC
          ) AS rn
        FROM symptoms s
        WHERE s.user_id = :userId
          AND (s.updated_at AT TIME ZONE 'Europe/Istanbul')::date >= :startDate
          AND (s.updated_at AT TIME ZONE 'Europe/Istanbul')::date <  :endDate  -- exclusive
      )
      SELECT COALESCE(SUM(steps), 0)
      FROM ranked
      WHERE rn = 1
      """, nativeQuery = true)
  Integer sumStepsOfLatestPerDayInRangePgByDate(
      @Param("userId") Long userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate // exclusive
  );

  List<Symptoms> findAllByUserIdAndCreatedAtBefore(Long userId, LocalDateTime before);

  @Query("""
          SELECT COALESCE(SUM(COALESCE(s.steps, 0)), 0)
          FROM Symptoms s
          WHERE s.user.id = :userId
            AND s.createdAt >= :start
            AND s.createdAt < :end
      """)
  Long sumStepsBetween(@Param("userId") Long userId,
      @Param("start") Timestamp start,
      @Param("end") Timestamp end);

  @Query(value = """
      WITH ranked AS (
        SELECT
          s.user_id,
          (s.updated_at AT TIME ZONE 'Europe/Istanbul')::date AS day_tr,
          COALESCE(s.steps, 0) AS steps,
          ROW_NUMBER() OVER (
            PARTITION BY s.user_id, (s.updated_at AT TIME ZONE 'Europe/Istanbul')::date
            ORDER BY s.updated_at DESC
          ) AS rn
        FROM symptoms s
        WHERE s.user_id = :userId
          AND (s.updated_at AT TIME ZONE 'Europe/Istanbul')::date < :currentMonday -- mevcut haftayı hariç tut
      ),
      daily_last AS (
        SELECT day_tr, steps
        FROM ranked
        WHERE rn = 1
      ),
      weekly AS (
        SELECT date_trunc('week', day_tr::timestamp) AS week_start,
               SUM(steps) AS week_steps
        FROM daily_last
        GROUP BY 1
      )
      SELECT COALESCE(AVG(week_steps), 0)
      FROM weekly
      """, nativeQuery = true)
  Double avgWeeklyStepsBeforeMondayLatestPerDay(
      @Param("userId") Long userId,
      @Param("currentMonday") LocalDate currentMonday);
}
