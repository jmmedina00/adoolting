package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.person.Notification;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.person.NotificationRepository;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private PageService pageService;

  public Page<Notification> getNotificationsForPerson(
    Long personId,
    Pageable pageable
  ) {
    return notificationRepository.findActiveNotificationsByPersonId(
      personId,
      pageable
    );
  }

  public void createNotifications(
    Interaction interaction,
    Interactor interactor
  ) {
    if (interactor instanceof Person) {
      createNotificationForPerson(interaction, (Person) interactor);
      return;
    }

    for (Person person : pageService.getPageManagers(interactor.getId())) {
      createNotificationForPerson(interaction, person);
    }
  }

  public Notification deleteNotification(Long notificationId, Long personId) {
    Notification notification = notificationRepository
      .findDeletableNotification(notificationId, personId)
      .orElseThrow();
    notification.setDeletedAt(new Date());
    return notificationRepository.save(notification);
  }

  private Notification createNotificationForPerson(
    Interaction interaction,
    Person person
  ) {
    Notification notification = new Notification();
    notification.setForPerson(person);
    notification.setInteraction(interaction);
    return notificationRepository.save(notification);
  }
}
