package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.enums.AccessLevel;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.group.JoinRequestService;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonAccessLevelService {
  @Autowired
  private InteractorService interactorService;

  @Autowired
  private InteractionService interactionService;

  @Autowired
  private ConfirmableInteractionService cInteractionService;

  @Autowired
  private PersonSettingsService settingsService;

  @Autowired
  private JoinRequestService joinRequestService;

  public AccessLevel getAccessLevelThatPersonHasOnInteractor(
    Long personId,
    Long otherInteractorId
  ) {
    if (Objects.equals(personId, otherInteractorId)) {
      return AccessLevel.OPEN;
    }

    Interactor otherInteractor = interactorService.getInteractor(
      otherInteractorId
    );

    if (otherInteractor instanceof Page) {
      return AccessLevel.OPEN;
    }

    ConfirmableInteraction friendship = cInteractionService.getPersonFriendship(
      personId,
      otherInteractorId
    );

    if (friendship != null) {
      return AccessLevel.OPEN;
    }

    if (
      !settingsService.isAllowedByPerson(
        otherInteractorId,
        PersonSettingsService.ENTER_PROFILE
      )
    ) {
      return AccessLevel.CLOSED;
    }

    if (
      !settingsService.isAllowedByPerson(
        otherInteractorId,
        PersonSettingsService.COMMENT_ON_INTERACTION
      )
    ) {
      return AccessLevel.WATCH_ONLY;
    }

    return AccessLevel.OPEN;
  }

  public AccessLevel getAccessLevelThatPersonHasOnInteraction(
    Long personId,
    Long interactionId
  ) {
    Interaction interaction = interactionService.getInteraction(interactionId);
    Interaction rootInteraction = interaction;

    while (rootInteraction instanceof Comment) {
      rootInteraction = ((Comment) rootInteraction).getReceiverInteraction();
    }

    return retrieveAccessLevelFromActualInteraction(personId, rootInteraction);
  }

  private AccessLevel retrieveAccessLevelFromActualInteraction(
    Long personId,
    Interaction interaction
  ) {
    if (interaction instanceof PeopleGroup) {
      if (joinRequestService.isMemberOfGroup(interaction.getId(), personId)) {
        return AccessLevel.OPEN;
      } else {
        return ((PeopleGroup) interaction).getAccessLevel();
      }
    }

    Interactor creator = interaction.getInteractor();
    Interactor receiver = interaction.getReceiverInteractor();

    List<Long> checkingInteractors = receiver == null
      ? List.of(creator.getId())
      : List.of(creator.getId(), receiver.getId());

    boolean personOwnsInteraction = checkingInteractors
      .stream()
      .filter(
        interactorId ->
          interactorService.isInteractorRepresentableByPerson(
            interactorId,
            personId
          )
      )
      .findFirst()
      .isPresent();

    if (personOwnsInteraction) {
      return AccessLevel.OPEN;
    }

    List<AccessLevel> accessLevels = checkingInteractors
      .stream()
      .map(
        interactorId ->
          getAccessLevelThatPersonHasOnInteractor(personId, interactorId)
      )
      .toList();

    if (accessLevels.size() == 1) {
      return accessLevels.get(0);
    }

    List<AccessLevel> open = accessLevels
      .stream()
      .filter(level -> level == AccessLevel.OPEN)
      .toList();
    List<AccessLevel> closed = accessLevels
      .stream()
      .filter(level -> level == AccessLevel.CLOSED)
      .toList();

    if (open.size() == accessLevels.size()) {
      return AccessLevel.OPEN;
    }

    if (closed.size() == accessLevels.size()) {
      return AccessLevel.CLOSED;
    }

    return AccessLevel.WATCH_ONLY;
  }
}
