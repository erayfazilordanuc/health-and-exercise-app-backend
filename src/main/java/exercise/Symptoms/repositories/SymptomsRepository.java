package exercise.Symptoms.repositories;

import java.sql.Timestamp;
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

    @Query("SELECT s FROM Symptoms s WHERE s.user.id = :userId AND DATE(s.updatedAt) = DATE(:date)")
    Symptoms findByUserIdAndDate(Long userId, Timestamp date);

    @Query("SELECT s FROM Symptoms s WHERE s.user.id = :userId AND s.updatedAt >= :startDate ORDER BY s.updatedAt DESC")
    List<Symptoms> findLastWeekByUserId(@Param("userId") Long userId,
            @Param("startDate") Timestamp startDate);

    List<Symptoms> findAllByUserIdAndCreatedAtBefore(Long userId, LocalDateTime before);
}
