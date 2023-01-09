package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.InteractionRepository;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.NotificationService;
import io.github.jmmedina00.adoolting.service.person.NotifiedInteractorService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  public Page<Interaction> getInteractionsFromInteractor(
    Long interactorId,
    Pageable pageable
  ) {
    return interactionRepository.findInteractionsByInteractorIds(
      List.of(interactorId),
      pageable
    );
  }

  public Page<Interaction> getInteractionsFromInteractors(
    List<Long> interactorIds,
    Pageable pageable
  ) {
    return interactionRepository.findInteractionsByInteractorIds(
      interactorIds,
      pageable
    );
  }

  public void deleteInteraction(Long interactionId, Long personId)
    throws NotAuthorizedException {
    Interaction interaction = getDeletableInteraction(interactionId);
    if (!checkIfInteractionDeletableByPerson(interaction, personId)) {
      throw new NotAuthorizedException();
    }

    interaction.setDeletedAt(new Date());

    logger.info(
      "Interactor {} has deleted interaction {}",
      personId,
      interactionId
    );
    interactionRepository.save(interaction);
  }

  public boolean isInteractionDeletableByPerson(
    Long interactionId,
    Long personId
  ) {
    try {
      Interaction interaction = getDeletableInteraction(interactionId);
      return checkIfInteractionDeletableByPerson(interaction, personId);
    } catch (NotAuthorizedException e) {
      return false;
    }
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

  private Interaction getDeletableInteraction(Long interactionId)
    throws NotAuthorizedException {
    return interactionRepository
      .findDeletableInteraction(interactionId)
      .orElseThrow(NotAuthorizedException::new);
  }

  private void addInteractorIdsToList(List<Long> ids, Interaction interaction) {
    ids.add(interaction.getInteractor().getId());

    if (interaction.getReceiverInteractor() != null) {
      ids.add(interaction.getReceiverInteractor().getId());
    }
  }

  private boolean checkIfInteractionDeletableByPerson(
    Interaction interaction,
    Long personId
  ) {
    if (interaction instanceof PeopleGroup) {
      logger.debug(
        "Interaction {} is a group. Cannot go through normal deletion",
        interaction.getId()
      );
      return false;
    }

    ArrayList<Long> involvedInteractors = new ArrayList<>();
    addInteractorIdsToList(involvedInteractors, interaction);

    if (interaction instanceof Comment) {
      logger.debug(
        "Interaction {} is a comment. Fetching original interaction interactors as well",
        interaction.getId()
      );
      addInteractorIdsToList(
        involvedInteractors,
        ((Comment) interaction).getReceiverInteraction()
      );
    }

    Optional<Long> idRepresentableByPerson = involvedInteractors
      .stream()
      .filter(
        id -> interactorService.isInteractorRepresentableByPerson(id, personId)
      )
      .findFirst();
    return idRepresentableByPerson.isPresent();
  }
}
