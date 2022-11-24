package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteractionRepository
  extends JpaRepository<Interaction, Long> {}
