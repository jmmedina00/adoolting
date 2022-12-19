package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConfirmableInteractionRepository
  extends JpaRepository<ConfirmableInteraction, Long> {
  @Query(
    "SELECT c FROM ConfirmableInteraction c WHERE " +
    "c.receiverInteractor.id=:interactorId AND c.confirmedAt IS NULL AND " +
    "c.ignoredAt IS NULL AND c.deletedAt IS NULL ORDER BY c.createdAt DESC"
  )
  List<ConfirmableInteraction> findPendingConfirmableInteractionsByInteractorId(
    @Param("interactorId") Long interactorId
  );

  @Query(
    "SELECT c FROM ConfirmableInteraction c WHERE c.id=:interactionId AND " +
    "c.receiverInteractor.id=:interactorId AND c.confirmedAt IS NULL AND " +
    "c.ignoredAt IS NULL AND c.deletedAt IS NULL ORDER BY c.createdAt DESC"
  )
  Optional<ConfirmableInteraction> findPendingConfirmableInteractionForInteractor(
    @Param("interactionId") Long interactionId,
    @Param("interactorId") Long interactorId
  );

  @Query(
    "SELECT c FROM ConfirmableInteraction c WHERE " +
    "(c.interactor.id=:interactorId OR c.receiverInteractor.id=:interactorId) AND " +
    "c.confirmedAt IS NOT NULL AND c.ignoredAt IS NULL AND c.deletedAt IS NULL AND " +
    "c.id NOT in (SELECT j.id FROM JoinRequest j) " +
    "ORDER BY c.createdAt DESC"
  )
  List<ConfirmableInteraction> findFriendsByInteractorId(
    @Param("interactorId") Long interactorId
  );

  @Query(
    "SELECT c FROM ConfirmableInteraction c WHERE " +
    "((c.interactor.id=:firstId AND c.receiverInteractor.id=:secondId) OR " +
    "(c.interactor.id=:secondId AND c.receiverInteractor.id=:firstId)) AND " +
    "c.id NOT in (SELECT j.id FROM JoinRequest j) AND " +
    "c.deletedAt IS NULL AND c.ignoredAt IS NULL " +
    "ORDER BY c.createdAt DESC"
  )
  ConfirmableInteraction findFriendshipBetweenInteractors(
    @Param("firstId") Long interactorId,
    @Param("secondId") Long otherInteractorId
  );
}
