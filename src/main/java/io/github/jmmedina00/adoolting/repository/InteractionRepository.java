package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InteractionRepository
  extends JpaRepository<Interaction, Long> {
  @Query(
    "SELECT i FROM Interaction i WHERE (i.interactor.id=:interactorId OR i.receiverInteractor.id=:interactorId) AND i.deletedAt IS NULL"
  )
  List<Interaction> findInteractionsByInteractorId(
    @Param("interactorId") Long interactorId
  );
}
