package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.person.Person;
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

  public ConfirmableInteraction getPersonFriendship(
    Long personId,
    Long otherPersonId
  ) {
    return cInteractionRepository.findFriendshipBetweenInteractors(
      personId,
      otherPersonId
    );
  }

  public List<Person> getPersonFriends(Long personId) {
    return cInteractionRepository
      .findFriendsByInteractorId(personId)
      .stream()
      .map(
        cInteraction -> {
          Person person = (Person) cInteraction.getInteractor();
          Person receiverPerson = (Person) cInteraction.getReceiverInteractor();
          return Objects.equals(personId, person.getId())
            ? receiverPerson
            : person;
        }
      )
      .toList();
  }

  public ConfirmableInteraction decideInteractionResult(
    Long interactionId,
    Long personId,
    boolean isAccepted
  )
    throws NotAuthorizedException {
    ConfirmableInteraction interaction = cInteractionRepository
      .findPendingConfirmableInteractionForInteractor(interactionId, personId)
      .orElseThrow(() -> new NotAuthorizedException());

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
    throws NotAuthorizedException {
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
      throw new NotAuthorizedException();
    }

    Person requestingPerson = personService.getPerson(requestingPersonId);
    Person addedPerson = personService.getPerson(addedPersonId);

    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setInteractor(requestingPerson);
    interaction.setReceiverInteractor(addedPerson);

    return cInteractionRepository.save(interaction);
  }
}
