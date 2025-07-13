package exercise.Exercise.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exercise.Exercise.entities.Achievement;
import exercise.Symptoms.entities.Symptoms;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

  public List<Achievement> findByUserId(Long userId);
}