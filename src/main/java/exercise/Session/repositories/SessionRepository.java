package exercise.Session.repositories;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import exercise.Session.entities.Session;
import exercise.Session.repositories.projections.DailySummaryProjection;

public interface SessionRepository extends JpaRepository<Session, Long> {
  Optional<Session> findBySessionId(UUID sessionId);

  @Query("""
      select s from Session s
      where s.user.id = :userId
        and s.startedAt >= :fromTs
        and s.startedAt <  :toTs
      order by s.startedAt desc
      """)
  List<Session> findByUserAndRange(Long userId,
      Timestamp fromTs,
      Timestamp toTs);

  // Günlük özet (native, PostgreSQL)
  @Query(value = """
      select date_trunc('day', started_at) as day,
             count(*) as sessions_count,
             coalesce(sum(active_ms),0) as total_active_ms
      from sessions
      where user_id = :userId
        and started_at >= now() - (:days || ' days')::interval
      group by day
      order by day desc
      """, nativeQuery = true)
  List<DailySummaryProjection> dailySummary(Long userId, int days);
}
