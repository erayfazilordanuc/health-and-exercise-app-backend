package exercise.Symptoms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import exercise.Symptoms.entities.Symptoms;

@Repository
public interface SymptomsRepository extends JpaRepository<Symptoms, Long> {

    @Query("SELECT symptoms FROM Symptoms symptoms WHERE symptoms.user.id = :userId ORDER BY symptoms.updatedAt DESC")
    public List<Symptoms> findByUserId(Long userId);
}
