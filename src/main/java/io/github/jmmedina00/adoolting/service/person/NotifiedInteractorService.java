package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotifiedInteractorService {
  @Autowired
  private PersonService personService;

  @Autowired
  private PageService pageService;

  private static final Logger logger = LoggerFactory.getLogger(
    NotifiedInteractorService.class
  );

  public Map<Person, Integer> getInteractorsInterestedInInteraction(
    Interaction interaction
  ) {
    Long interactionId = interaction.getId();

    if (interaction.getDeletedAt() != null) {
      logger.debug(
        "Interaction {} was deleted. No one to be notified.",
        interactionId
      );
      return Collections.emptyMap();
    }

    Interactor author = (Interactor) Hibernate.unproxy(
      interaction.getInteractor()
    );
    Interactor receiver = (Interactor) Hibernate.unproxy(
      interaction.getReceiverInteractor()
    );

    if (interaction instanceof ConfirmableInteraction) {
      ConfirmableInteraction cInteraction = (ConfirmableInteraction) interaction;
      logger.debug("Interaction {} is confirmable", interactionId);
      if (cInteraction.getIgnoredAt() != null) {
        logger.debug(
          "Confirmable {} ignored. No one to be notified.",
          interactionId
        );
        return Collections.emptyMap();
      }

      return cInteraction.getConfirmedAt() == null
        ? getMapOfSingleInteractor(receiver)
        : getMapOfSingleInteractor(author);
    }

    HashMap<Person, Integer> interestedPersons = new HashMap<>();

    if (interaction instanceof Comment) {
      Comment comment = (Comment) interaction;
      Interaction commentedOnInteraction = comment.getReceiverInteraction();
      Long originalInteractorId = commentedOnInteraction
        .getInteractor()
        .getId();
      logger.debug(
        "Interaction {} is a comment on other interaction {}",
        interactionId,
        commentedOnInteraction.getId()
      );

      if (Objects.equals(originalInteractorId, author.getId())) {
        logger.debug(
          "Comment {} was done by author's interaction. Skipping notification.",
          interactionId
        );
      } else {
        logger.debug(
          "Notifying interactor {} of comment {}",
          originalInteractorId,
          interactionId
        );
        addInteractorToNotificationMap(
          interestedPersons,
          commentedOnInteraction.getInteractor(),
          PersonSettingsService.NOTIFY_COMMENT
        );
      }
    }

    if (author instanceof Page) {
      logger.debug(
        "People who liked page {} to be notified of interaction {}.",
        author.getId(),
        interactionId
      );

      for (Person person : personService.getPersonsWhoLikedPage(
        author.getId()
      )) {
        interestedPersons.putIfAbsent(
          person,
          PersonSettingsService.NOTIFY_PAGE_INTERACTION
        );
      }
    }

    if (receiver != null) {
      logger.debug(
        "Interaction {} received by interactor {}. Notifying them as well.",
        interactionId,
        receiver.getId()
      );
      addInteractorToNotificationMap(
        interestedPersons,
        receiver,
        PersonSettingsService.NOTIFY_POST_FROM_OTHER
      );
    }

    return interestedPersons;
  }

  private Map<Person, Integer> getMapOfSingleInteractor(Interactor interactor) {
    if (interactor instanceof Person) {
      return Map.of((Person) interactor, 0);
    }

    List<Person> pageManagers = pageService.getPageManagers(interactor.getId());
    HashMap<Person, Integer> map = new HashMap<>();

    for (Person person : pageManagers) {
      map.put(person, 0);
    }

    return map;
  }

  private void addInteractorToNotificationMap(
    HashMap<Person, Integer> map,
    Interactor interactor,
    int code
  ) {
    if (interactor instanceof Person) {
      map.put((Person) interactor, code);
      return;
    }

    List<Person> pageManagers = pageService.getPageManagers(interactor.getId());

    for (Person person : pageManagers) {
      map.put(person, code);
    }
  }
}
