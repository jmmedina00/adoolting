package io.github.jmmedina00.adoolting.repository.person;

import io.github.jmmedina00.adoolting.entity.person.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository
  extends JpaRepository<Notification, Long> {
  @Query(
    "SELECT n FROM Notification n WHERE n.id=:notificationId " +
    "AND n.forPerson.id=:personId"
  )
  Optional<Notification> findBelongingNotification(
    @Param("notificationId") Long notificationId,
    @Param("personId") Long personId
  );

  @Query(
    "SELECT n FROM Notification n WHERE n.forPerson.id=:personId AND " +
    "n.deletedAt IS NULL ORDER BY n.createdAt DESC"
  )
  Page<Notification> findActiveNotificationsByPersonId(
    @Param("personId") Long personId,
    Pageable pageable
  );

  @Transactional
  @Modifying
  @Query(
    "UPDATE Notification n SET n.deletedAt = CURRENT_TIMESTAMP WHERE " +
    "n.interaction.id=:interactionId AND n.deletedAt IS NULL"
  )
  void deleteAllByInteractionId(@Param("interactionId") Long interactionId);
}
