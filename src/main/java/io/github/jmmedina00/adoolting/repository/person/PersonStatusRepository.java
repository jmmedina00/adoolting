package io.github.jmmedina00.adoolting.repository.person;

import io.github.jmmedina00.adoolting.entity.person.PersonStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonStatusRepository
  extends JpaRepository<PersonStatus, Long> {
  List<PersonStatus> findFirst1ByPersonIdOrderByCreatedAtDesc(Long personId);
}
