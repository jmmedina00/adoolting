package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.enums.NotificationSetting;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.person.Notification;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.person.NotificationRepository;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.Date;
import java.util.Objects;
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

  @Autowired
  private PersonSettingsService settingsService;

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

  private void createNotificationForPerson(
    Interaction interaction,
    Person person
  ) {
    Long personId = person.getId();
    int code = (interaction instanceof Comment)
      ? PersonSettingsService.NOTIFY_COMMENT
      : (interaction.getInteractor() instanceof Page)
        ? PersonSettingsService.NOTIFY_PAGE_INTERACTION
        : PersonSettingsService.NOTIFY_POST_FROM_OTHER;
    NotificationSetting setting = settingsService.getNotificationSetting(
      personId,
      code
    );

    if (
      !(interaction instanceof ConfirmableInteraction) &&
      setting == NotificationSetting.NONE
    ) {
      return;
    }

    Notification notification = new Notification();
    notification.setForPerson(person);
    notification.setInteraction(interaction);
    notificationRepository.save(notification);

    if (interaction instanceof ConfirmableInteraction) {
      emailOnConfirmable((ConfirmableInteraction) interaction, person);
    }

    if (setting == NotificationSetting.EMAIL) {
      System.out.println(
        "Email will be sent to " +
        person.getFullName() +
        " for " +
        interaction.getId()
      );
    }
  }

  private void emailOnConfirmable(
    ConfirmableInteraction interaction,
    Person person
  ) {
    Long personId = interaction.getReceiverInteractor().getId();

    if (!Objects.equals(person.getId(), personId)) {
      return;
    }

    if (
      !settingsService.isAllowedByPerson(
        personId,
        PersonSettingsService.EMAIL_CONFIRMABLE
      )
    ) {
      return;
    }

    System.out.println(
      "Sending email to " + personId + " on " + interaction.getId()
    );
  }
}
