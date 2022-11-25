package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConfirmableInteractionRepository
  extends JpaRepository<ConfirmableInteraction, Long> {
  @Query(
    "SELECT c FROM ConfirmableInteraction c WHERE (c.interactor.id=:interactorId OR c.receiverInteractor.id=:interactorId) AND c.deletedAt IS NULL"
  )
  List<ConfirmableInteraction> findConfirmableInteractionsByInteractorId(
    @Param("interactorId") Long interactorId
  );
}
