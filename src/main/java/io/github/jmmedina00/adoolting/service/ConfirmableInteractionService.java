package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.ConfirmableInteractionRepository;
import io.github.jmmedina00.adoolting.service.person.NotificationService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfirmableInteractionService {
  @Autowired
  private ConfirmableInteractionRepository cInteractionRepository;

  @Autowired
  private PersonService personService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private InteractionService interactionService;

  @Autowired
  private InteractorService interactorService;

  private static final Logger logger = LoggerFactory.getLogger(
    ConfirmableInteractionService.class
  );

  public ConfirmableInteraction getConfirmableInteraction(Long interactionId) {
    return cInteractionRepository.findById(interactionId).orElseThrow();
  } // Needed for certain email templates

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
      .findById(interactionId)
      .orElseThrow(NotAuthorizedException::new);

    Long receiverId = interaction.getReceiverInteractor().getId();

    if (
      !interactorService.isInteractorRepresentableByPerson(receiverId, personId)
    ) {
      throw new NotAuthorizedException();
    }

    Date now = new Date();
    if (isAccepted) {
      logger.info(
        "Person {} has confirmed interaction {}",
        personId,
        interactionId
      );
      interaction.setConfirmedAt(now);
    } else {
      logger.info(
        "Person {} has ignored interaction {}",
        personId,
        interactionId
      );
      interaction.setIgnoredAt(now);
    }

    notificationService.deleteInteractionNotifications(interactionId);

    return (ConfirmableInteraction) interactionService.saveInteraction(
      interaction
    );
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

    logger.debug(
      "No confirmed nor pending interaction between persons {} and {} found. We can proceed",
      requestingPersonId,
      addedPersonId
    );

    Person requestingPerson = personService.getPerson(requestingPersonId);
    Person addedPerson = personService.getPerson(addedPersonId);

    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setInteractor(requestingPerson);
    interaction.setReceiverInteractor(addedPerson);
    ConfirmableInteraction saved = (ConfirmableInteraction) interactionService.saveInteraction(
      interaction
    );

    logger.info(
      "New friendship request created between {} and {}, id is {}",
      requestingPersonId,
      addedPersonId,
      saved.getId()
    );
    return saved;
  }

  public Map<Person, Long> getPersonFriendsOfFriends(Long personId) {
    List<Person> friends = getPersonFriends(personId);

    List<Person> friendsOfFriends = friends
      .stream()
      .flatMap(person -> getPersonFriends(person.getId()).stream())
      .filter(
        person ->
          !(
            Objects.equals(personId, person.getId()) ||
            friends.indexOf(person) >= 0
          )
      )
      .toList();

    HashMap<Person, Long> personOccurrences = new HashMap<>();

    for (Person person : friendsOfFriends) {
      personOccurrences.merge(person, 1L, Long::sum);
    }

    return personOccurrences
      .entrySet()
      .stream()
      .sorted(Entry.comparingByValue((a, b) -> -Long.compare(a, b)))
      .collect(
        Collectors.toMap(
          Entry::getKey,
          Entry::getValue,
          (a, b) -> a,
          LinkedHashMap::new
        )
      );
  }
}
