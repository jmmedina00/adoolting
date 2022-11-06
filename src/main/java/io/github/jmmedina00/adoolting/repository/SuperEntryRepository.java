package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.SuperEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuperEntryRepository extends JpaRepository<SuperEntry, Long> {
  List<SuperEntry> findByDeletedAtIsNull();
}
