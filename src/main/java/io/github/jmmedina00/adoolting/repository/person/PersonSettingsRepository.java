package io.github.jmmedina00.adoolting.repository.person;

import io.github.jmmedina00.adoolting.entity.person.PersonSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonSettingsRepository
  extends JpaRepository<PersonSettings, Long> {
  @Query("SELECT s FROM PersonSettings s WHERE s.person.id=:personId")
  Optional<PersonSettings> findByPersonId(@Param("personId") Long personId);
}
