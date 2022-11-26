package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.JoinRequest;
import io.github.jmmedina00.adoolting.entity.Person;
import io.github.jmmedina00.adoolting.exception.InvalidDTOException;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.ConfirmableInteractionRepository;
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
    Person person
  ) {
    return cInteractionRepository.findPendingConfirmableInteractionsByInteractorId(
      person.getId()
    );
  }

  public List<ConfirmableInteraction> getPersonFriends(Person person) {
    return null;
  }

  public ConfirmableInteraction getPersonFriendship(
    Person person,
    Person otherPerson
  ) {
    List<ConfirmableInteraction> interactions = cInteractionRepository.findConfirmableInteractionsBetweenInteractors(
      person.getId(),
      otherPerson.getId()
    );

    return interactions
      .stream()
      .filter(interaction -> !(interaction instanceof JoinRequest))
      .findFirst()
      .orElse(null);
  }

  public ConfirmableInteraction decideInteractionResult(
    Long interactionId,
    Person person,
    boolean isAccepted
  )
    throws NotAuthorizedException {
    ConfirmableInteraction interaction = cInteractionRepository
      .findById(interactionId)
      .orElseThrow(() -> new NotAuthorizedException());

    if (
      !Objects.equals(
        person.getId(),
        interaction.getReceiverInteractor().getId()
      )
    ) {
      throw new NotAuthorizedException();
    }

    if (
      interaction.getConfirmedAt() != null && interaction.getIgnoredAt() != null
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
    Person requestingPerson,
    Long addedPersonId
  )
    throws InvalidDTOException {
    Person addedPerson = personService.getPerson(addedPersonId);

    ConfirmableInteraction existingFriendship = getPersonFriendship(
      requestingPerson,
      addedPerson
    );
    if (
      existingFriendship != null && existingFriendship.getConfirmedAt() != null
    ) {
      throw new InvalidDTOException();
    }

    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setInteractor(requestingPerson);
    interaction.setReceiverInteractor(addedPerson);

    return cInteractionRepository.save(interaction);
  }
}
