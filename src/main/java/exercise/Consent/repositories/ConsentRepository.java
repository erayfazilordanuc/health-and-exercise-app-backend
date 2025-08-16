package exercise.Consent.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import exercise.Consent.entities.Consent;
import exercise.Consent.enums.ConsentPurpose;

public interface ConsentRepository extends JpaRepository<Consent, Long> {
  List<Consent> findByUser_Id(Long userId);

  Optional<Consent> findByUser_IdAndPurpose(Long userId, ConsentPurpose purpose);
}
