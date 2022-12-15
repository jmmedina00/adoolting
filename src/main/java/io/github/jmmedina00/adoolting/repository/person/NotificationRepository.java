package io.github.jmmedina00.adoolting.repository.person;

import io.github.jmmedina00.adoolting.entity.person.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository
  extends JpaRepository<Notification, Long> {
  @Query(
    "SELECT n FROM Notification n WHERE n.id=:notificationId " +
    "AND n.forPerson.id=:personId"
  )
  Optional<Notification> findDeletableNotification(
    @Param("notificationId") Long notificationId,
    @Param("personId") Long personId
  );

  @Query(
    "SELECT n FROM Notification n WHERE n.forPerson.id=:personId AND " +
    "n.interaction.id NOT IN (SELECT c.id FROM ConfirmableInteraction c WHERE " +
    "c.receiverInteractor.id=:personId AND (c.confirmedAt IS NOT NULL OR " +
    "c.ignoredAt IS NOT NULL)) AND n.interaction.id NOT IN " +
    "(SELECT c.id FROM ConfirmableInteraction c WHERE c.interactor.id=:personId AND " +
    "c.confirmedAt IS NULL) AND n.interaction.deletedAt IS NULL AND " +
    "n.deletedAt IS NULL ORDER BY n.createdAt DESC"
  )
  Page<Notification> findActiveNotificationsByPersonId(
    @Param("personId") Long personId,
    Pageable pageable
  );
}
