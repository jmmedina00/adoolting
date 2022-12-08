package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.InvalidDTOException;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.ConfirmableInteractionRepository;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfirmableInteractionService {
  @Autowired
  private ConfirmableInteractionRepository cInteractionRepository;

  @Autowired
  private PersonService personService;

  public List<ConfirmableInteraction> getPendingInteractionsForPerson(
    Long personId
  ) {
    return cInteractionRepository.findPendingConfirmableInteractionsByInteractorId(
      personId
    );
  }

  public List<ConfirmableInteraction> getPersonFriends(Long personId) {
    List<ConfirmableInteraction> interactions = cInteractionRepository.findConfirmedInteractionsByInteractorId(
      personId
    );
    return interactions
      .stream()
      .filter(interaction -> !(interaction instanceof JoinRequest))
      .toList();
  }

  public ConfirmableInteraction getPersonFriendship(
    Long personId,
    Long otherPersonId
  ) {
    List<ConfirmableInteraction> interactions = cInteractionRepository.findConfirmableInteractionsBetweenInteractors(
      personId,
      otherPersonId
    );

    return interactions
      .stream()
      .filter(interaction -> !(interaction instanceof JoinRequest))
      .findFirst()
      .orElse(null);
  }

  public ConfirmableInteraction decideInteractionResult(
    Long interactionId,
    Long personId,
    boolean isAccepted
  )
    throws NotAuthorizedException {
    ConfirmableInteraction interaction = cInteractionRepository
      .findById(interactionId)
      .orElseThrow(() -> new NotAuthorizedException());

    if (
      !Objects.equals(personId, interaction.getReceiverInteractor().getId())
    ) {
      throw new NotAuthorizedException();
    }

    if (
      interaction.getConfirmedAt() != null || interaction.getIgnoredAt() != null
    ) {
      throw new NotAuthorizedException();
    }

    Date now = new Date();
    if (isAccepted) {
      interaction.setConfirmedAt(now);
    } else {
      interaction.setIgnoredAt(now);
    }

    return cInteractionRepository.save(interaction);
  }

  public ConfirmableInteraction addPersonAsFriend(
    Long requestingPersonId,
    Long addedPersonId
  )
    throws InvalidDTOException {
    ConfirmableInteraction existingFriendship = getPersonFriendship(
      requestingPersonId,
      addedPersonId
    );
    if (
      existingFriendship != null &&
      (
        existingFriendship.getConfirmedAt() != null ||
        existingFriendship.getIgnoredAt() == null
      )
    ) {
      throw new InvalidDTOException();
    }

    Person requestingPerson = personService.getPerson(requestingPersonId);
    Person addedPerson = personService.getPerson(addedPersonId);

    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setInteractor(requestingPerson);
    interaction.setReceiverInteractor(addedPerson);

    return cInteractionRepository.save(interaction);
  }
}
