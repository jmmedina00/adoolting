package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfirmableInteractionRepository
  extends JpaRepository<ConfirmableInteraction, Long> {}
