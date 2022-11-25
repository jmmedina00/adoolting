package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.JoinRequest;
import io.github.jmmedina00.adoolting.entity.Person;
import io.github.jmmedina00.adoolting.repository.ConfirmableInteractionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfirmableInteractionService {
  @Autowired
  private ConfirmableInteractionRepository cInteractionRepository;

  public List<ConfirmableInteraction> getPendingInteractionsForPerson(
    Person person
  ) {
    return cInteractionRepository.findConfirmableInteractionsByInteractorId(
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
  ) {
    return null;
  }

  public ConfirmableInteraction addPersonAsFriend(
    Person requestingPerson,
    Person addedPerson
  ) {
    return null;
  }
}
