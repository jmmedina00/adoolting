package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.Medium;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediumRepository extends JpaRepository<Medium, Long> {
  List<Medium> findByInteractionId(Long interactionId);
}
