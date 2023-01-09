package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InteractionRepository
  extends JpaRepository<Interaction, Long> {
  @Query(
    "SELECT i FROM Interaction i WHERE i.id=:interactionId AND " +
    "i.deletedAt IS NULL AND i.id NOT IN " +
    "(SELECT c.id FROM ConfirmableInteraction c WHERE c.confirmedAt IS NULL)"
  )
  Optional<Interaction> findActiveInteraction(
    @Param("interactionId") Long interactionId
  );

  @Query(
    "SELECT i FROM Interaction i WHERE i.id=:interactionId AND " +
    "i.id NOT IN (SELECT g.id FROM PeopleGroup g) AND " +
    "i.deletedAt IS NULL AND i.id NOT IN " +
    "(SELECT c.id FROM ConfirmableInteraction c WHERE c.confirmedAt IS NULL)"
  )
  Optional<Interaction> findDeletableInteraction(
    @Param("interactionId") Long interactionId
  );

  @Query(
    "SELECT i FROM Interaction i WHERE " +
    "(i.interactor.id IN (:interactorIds) OR i.receiverInteractor.id IN (:interactorIds)) " +
    "AND i.deletedAt IS NULL AND i.id NOT IN " +
    "(SELECT c.id FROM ConfirmableInteraction c WHERE c.confirmedAt IS NULL) " +
    "ORDER BY i.createdAt DESC"
  )
  Page<Interaction> findInteractionsByInteractorIds(
    @Param("interactorIds") List<Long> interactorIds,
    Pageable pageable
  );
}
