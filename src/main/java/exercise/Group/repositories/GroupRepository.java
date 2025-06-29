package exercise.Group.repositories;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import exercise.Group.entities.Group;
import exercise.Symptoms.entities.Symptoms;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
}
