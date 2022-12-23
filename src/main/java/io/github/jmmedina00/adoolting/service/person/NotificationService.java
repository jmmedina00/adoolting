package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.enums.NotificationSetting;
import io.github.jmmedina00.adoolting.entity.person.Notification;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.person.NotificationRepository;
import io.github.jmmedina00.adoolting.service.util.EmailService;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private PersonSettingsService settingsService;

  @Autowired
  private EmailService emailService;

  private static final Logger logger = LoggerFactory.getLogger(
    NotificationService.class
  );

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
    Person person,
    int code
  ) {
    logger.debug(
      "Start notifying interaction id {} to person {}",
      interaction.getId(),
      person.getId()
    );
    notifyPersonIfWanted(interaction, person, code);
  }

  public Notification deleteNotification(Long notificationId, Long personId) {
    Notification notification = notificationRepository
      .findDeletableNotification(notificationId, personId)
      .orElseThrow();
    notification.setDeletedAt(new Date());
    logger.info("Deleting notification {}");
    return notificationRepository.save(notification);
  }

  private void notifyPersonIfWanted(
    Interaction interaction,
    Person person,
    int code
  ) {
    Long personId = person.getId();
    Long interactionId = interaction.getId();

    if (interaction instanceof ConfirmableInteraction) {
      logger.debug(
        "Interaction {} is confirmable, resorting to confirmable flow",
        interactionId
      );
      notifyConfirmable((ConfirmableInteraction) interaction, person);
      return;
    }

    if (Objects.equals(personId, interaction.getInteractor().getId())) {
      logger.debug(
        "Person {} is author of interaction {}. Skipping notification.",
        personId,
        interaction.getId()
      );
      return;
    }

    NotificationSetting setting = settingsService.getNotificationSetting(
      personId,
      code
    );
    logger.debug("Person {}'s code {} set to {}", personId, code, setting);

    if (setting == NotificationSetting.NONE) {
      logger.debug(
        "No notification wanted by person {} on interaction {}",
        personId,
        interactionId
      );
      return;
    }

    Notification notification = createNotification(interaction, person);

    if (setting == NotificationSetting.EMAIL) {
      logger.debug(
        "Notification {} will also be emailed",
        notification.getId()
      );

      String template;

      switch (code) {
        case PersonSettingsService.NOTIFY_COMMENT:
          template = "comment";
          break;
        case PersonSettingsService.NOTIFY_PAGE_INTERACTION:
          template = "page-activity";
          break;
        case PersonSettingsService.NOTIFY_POST_FROM_OTHER:
        default:
          template = "new-post";
          break;
      }

      emailService.setUpEmailJob(notification, template);
    }
  }

  private void notifyConfirmable(
    ConfirmableInteraction interaction,
    Person person
  ) {
    Long personId = person.getId();
    Notification notification = createNotification(interaction, person);

    if (
      !settingsService.isAllowedByPerson(
        personId,
        PersonSettingsService.EMAIL_CONFIRMABLE
      )
    ) {
      logger.debug(
        "Person {} to not be emailed about notification {}",
        person.getId(),
        notification.getId()
      );
      return;
    }

    logger.debug(
      "Email on notification {} to be sent to person {}",
      notification.getId(),
      person.getId()
    );
    List<Long> interactorIds = List.of(
      interaction.getInteractor().getId(),
      interaction.getReceiverInteractor().getId()
    );
    int position = interactorIds.indexOf(person.getId());
    String wantedTemplate = (position == 1) ? "pending" : "accepted"; // Assuming anything other than 1 to be (manager of) interactor

    logger.debug(
      "Wanted template for notification {} is {}",
      notification.getId(),
      wantedTemplate
    );

    emailService.setUpEmailJob(notification, wantedTemplate);
  }

  private Notification createNotification(
    Interaction interaction,
    Person person
  ) {
    Notification notification = new Notification();
    notification.setForPerson(person);
    notification.setInteraction(interaction);
    Notification saved = notificationRepository.save(notification);
    logger.info(
      "Created notification {} on interaction {} for person {}",
      notification.getId(),
      interaction.getId(),
      person.getId()
    );

    return saved;
  }
}
