package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.enums.NotificationSetting;
import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import io.github.jmmedina00.adoolting.entity.person.Notification;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.person.NotificationRepository;
import io.github.jmmedina00.adoolting.service.InteractorService;
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

  @Autowired
  private InteractorService interactorService;

  private static final Logger logger = LoggerFactory.getLogger(
    NotificationService.class
  );

  public void deleteInteractionNotifications(Long interactionId) {
    notificationRepository.deleteAllByInteractionId(interactionId);
  }

  public Page<Notification> getNotificationsForPerson(
    Long personId,
    Pageable pageable
  ) {
    return notificationRepository.findActiveNotificationsByPersonId(
      personId,
      pageable
    );
  }

  public Notification markNotificationAsRead(
    Long notificationId,
    Long personId
  ) {
    Notification notification = notificationRepository
      .findBelongingNotification(notificationId, personId)
      .orElseThrow();
    if (notification.getReadAt() == null) {
      notification.setReadAt(new Date());
      logger.info(
        "Notification {} is now read by person {}.",
        notificationId,
        personId
      );
    }
    return notificationRepository.save(notification);
  }

  public Notification deleteNotification(Long notificationId, Long personId) {
    Notification notification = notificationRepository
      .findBelongingNotification(notificationId, personId)
      .orElseThrow();
    notification.setDeletedAt(new Date());
    logger.info(
      "Person {} is now deleting notification {}",
      personId,
      notificationId
    );
    return notificationRepository.save(notification);
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

    if (interaction instanceof ConfirmableInteraction) {
      logger.debug(
        "Interaction {} is confirmable, using special flow",
        interaction.getId()
      );
      notifyConfirmable((ConfirmableInteraction) interaction, person);
    } else {
      logger.debug(
        "Interaction {} is not confirmable, using normal flow.",
        interaction.getId()
      );
      notifyPersonIfWanted(interaction, person, code);
    }
  }

  private void notifyPersonIfWanted(
    Interaction interaction,
    Person person,
    int code
  ) {
    Long personId = person.getId();
    Long interactionId = interaction.getId();

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

      emailNotification(notification, code);
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
    int position = interactorIds.indexOf(
      getInterestingInteractorIdToLookFor(notification)
    );
    String wantedTemplate = (position == 1) ? "pending" : "accepted";

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

  private void emailNotification(
    Notification notification,
    int notificationCode
  ) {
    String template;

    switch (notificationCode) {
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

  private Long getInterestingInteractorIdToLookFor(Notification notification) {
    Long personId = notification.getForPerson().getId();
    ConfirmableInteraction interaction = (ConfirmableInteraction) notification.getInteraction();

    if (!(interaction instanceof JoinRequest)) {
      return personId;
    }

    Long groupCreatorId =
      ((JoinRequest) interaction).getGroup().getInteractor().getId();
    return interactorService.isInteractorRepresentableByPerson(
        groupCreatorId,
        personId
      )
      ? groupCreatorId
      : personId;
  }
}
