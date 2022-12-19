package io.github.jmmedina00.adoolting.repository.person;

import io.github.jmmedina00.adoolting.entity.person.PersonSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonSettingsRepository
  extends JpaRepository<PersonSettings, Long> {}
