package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.InteractionRepository;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.NotificationService;
import io.github.jmmedina00.adoolting.service.person.NotifiedInteractorService;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InteractionService {
  @Autowired
  private InteractionRepository interactionRepository;

  @Autowired
  private NotificationService notificationService; // ALL INTERACTIONS GO THROUGH HERE OwO

  @Autowired
  private NotifiedInteractorService notifiedInteractorService;

  @Autowired
  private InteractorService interactorService;

  @Autowired
  private PageService pageService;

  private static final Logger logger = LoggerFactory.getLogger(
    InteractionService.class
  );

  public Interaction saveInteraction(Interaction interaction) {
    Interaction saved = interactionRepository.save(interaction);

    Map<Person, Integer> interestedInteractors = notifiedInteractorService.getInteractorsInterestedInInteraction(
      saved
    );
    for (Map.Entry<Person, Integer> entry : interestedInteractors.entrySet()) {
      notificationService.createNotifications(
        interaction,
        entry.getKey(),
        entry.getValue()
      );
    }

    return saved;
  }

  public Interaction getInteraction(Long interactionId) {
    return interactionRepository
      .findActiveInteraction(interactionId)
      .orElseThrow();
  }

  public Page<Interaction> getInteractions(
    Long interactorId,
    Pageable pageable
  ) {
    return interactionRepository.findInteractionsByInteractorId(
      interactorId,
      pageable
    );
  }

  public void deleteInteraction(Long interactionId, Long interactorId)
    throws NotAuthorizedException {
    Interaction interaction = interactionRepository
      .findDeletableInteractionForInteractor(interactionId, interactorId)
      .orElseThrow();

    interaction.setDeletedAt(new Date());
    interactionRepository.save(interaction);
  }

  public List<Interactor> getAppropriateInteractorListForPerson(
    Long personId,
    Long interactionId
  ) {
    Person person = (Person) interactorService.getInteractor(personId);
    Interaction interaction = getInteraction(interactionId);

    logger.debug(
      "Getting person {}'s adequate interactors for interaction {}",
      personId,
      interactionId
    );

    if (interaction instanceof Comment) {
      logger.debug(
        "Interaction {} is a comment. Commented interaction's creator is allowed to comment.",
        interactionId
      );
      Comment comment = (Comment) interaction;
      Interaction receiverInteraction = comment.getReceiverInteraction();
      Interactor receiverInteractor = receiverInteraction.getInteractor();
      return pageService.isPageManagedByPerson(
          receiverInteractor.getId(),
          personId
        )
        ? List.of(person, receiverInteractor)
        : List.of(person);
    }

    Interactor interactor = interaction.getInteractor();
    Interactor receiverInteractor = interaction.getReceiverInteractor();

    if (
      receiverInteractor instanceof io.github.jmmedina00.adoolting.entity.page.Page
    ) {
      logger.debug(
        "Interaction {}'s interactor {} is a page",
        interactionId,
        receiverInteractor.getId()
      );
      if (
        pageService.isPageManagedByPerson(receiverInteractor.getId(), personId)
      ) {
        logger.debug("Person manages and may comment as this page");
        return List.of(person, receiverInteractor);
      }
    }

    logger.debug(
      "Defaulting to presentable interactors in front of {}",
      interactor.getId()
    );

    return interactorService.getRepresentableInteractorsByPerson(
      personId,
      interactor.getId()
    );
  }
}
