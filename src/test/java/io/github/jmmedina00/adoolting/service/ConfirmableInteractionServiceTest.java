package io.github.jmmedina00.adoolting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.ConfirmableInteractionRepository;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class ConfirmableInteractionServiceTest {
  @MockBean
  private ConfirmableInteractionRepository cInteractionRepository;

  @MockBean
  private PersonService personService;

  @MockBean
  private InteractionService interactionService;

  @Autowired
  private ConfirmableInteractionService cInteractionService;

  @Test
  public void getPersonFriendsAlwaysGetsPersonDifferentFromProvidedInTheList() {
    Person interesting = new Person();
    interesting.setId(5L);

    Person foo = new Person();
    foo.setId(7L);
    Person bar = new Person();
    bar.setId(12L);
    Person baz = new Person();
    baz.setId(8L);

    ConfirmableInteraction fooIn = new ConfirmableInteraction();
    fooIn.setInteractor(interesting);
    fooIn.setReceiverInteractor(foo);
    ConfirmableInteraction barIn = new ConfirmableInteraction();
    barIn.setInteractor(bar);
    barIn.setReceiverInteractor(interesting);
    ConfirmableInteraction bazIn = new ConfirmableInteraction();
    bazIn.setInteractor(interesting);
    bazIn.setReceiverInteractor(baz);

    Mockito
      .when(cInteractionRepository.findFriendsByInteractorId(5L))
      .thenReturn(List.of(fooIn, barIn, bazIn));

    List<Person> friends = cInteractionService.getPersonFriends(5L);
    assertEquals(List.of(foo, bar, baz), friends);
  }

  @Test
  public void decideInteractionResultSetsConfirmedIfAcceptedAndSavesThroughInteractionService()
    throws NotAuthorizedException {
    ConfirmableInteraction interaction = new ConfirmableInteraction();

    Mockito
      .when(
        cInteractionRepository.findPendingConfirmableInteractionForInteractor(
          20L,
          7L
        )
      )
      .thenReturn(Optional.of(interaction));
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    ConfirmableInteraction saved = cInteractionService.decideInteractionResult(
      20L,
      7L,
      true
    );
    assertEquals(interaction, saved);
    assertNull(saved.getIgnoredAt());
    assertNotNull(saved.getConfirmedAt());
  }

  @Test
  public void decideInteractionResultSetsIgnoredAtIfIgnoredAndSavesThroughInteractionService()
    throws NotAuthorizedException {
    ConfirmableInteraction interaction = new ConfirmableInteraction();

    Mockito
      .when(
        cInteractionRepository.findPendingConfirmableInteractionForInteractor(
          20L,
          7L
        )
      )
      .thenReturn(Optional.of(interaction));
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    ConfirmableInteraction saved = cInteractionService.decideInteractionResult(
      20L,
      7L,
      false
    );
    assertEquals(interaction, saved);
    assertNotNull(saved.getIgnoredAt());
    assertNull(saved.getConfirmedAt());
  }

  @Test
  public void decideInteractionResultThrowsIfInteractionCannotBefound() {
    Mockito
      .when(
        cInteractionRepository.findPendingConfirmableInteractionForInteractor(
          20L,
          7L
        )
      )
      .thenReturn(Optional.empty());

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        cInteractionService.decideInteractionResult(20L, 7L, false);
      }
    );

    verify(interactionService, never()).saveInteraction(any());
  }

  @Test
  public void addPersonAsFriendCreatesNewConfirmableInteraction()
    throws NotAuthorizedException {
    Mockito
      .when(cInteractionRepository.findFriendshipBetweenInteractors(12L, 15L))
      .thenReturn(null);

    Person foo = new Person();
    Person bar = new Person();

    Mockito.when(personService.getPerson(12L)).thenReturn(foo);
    Mockito.when(personService.getPerson(15L)).thenReturn(bar);
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    ConfirmableInteraction interaction = cInteractionService.addPersonAsFriend(
      12L,
      15L
    );
    assertEquals(foo, interaction.getInteractor());
    assertEquals(bar, interaction.getReceiverInteractor());
  }

  @Test
  public void addPersonAsFriendCreatesInteractionWhenThereIsAlreadyARejectedInteraction()
    throws NotAuthorizedException {
    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setIgnoredAt(new Date(400L));

    Mockito
      .when(cInteractionRepository.findFriendshipBetweenInteractors(15L, 12L))
      .thenReturn(interaction);

    Person foo = new Person();
    Person bar = new Person();

    Mockito.when(personService.getPerson(12L)).thenReturn(foo);
    Mockito.when(personService.getPerson(15L)).thenReturn(bar);
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    ConfirmableInteraction saved = cInteractionService.addPersonAsFriend(
      15L,
      12L
    );
    assertEquals(bar, saved.getInteractor());
    assertEquals(foo, saved.getReceiverInteractor());
    assertNotEquals(interaction, saved);
  }

  @Test
  public void addPersonAsFriendThrowsWhenThereIsAlreadyAPendingInteractionExisting() {
    ConfirmableInteraction interaction = new ConfirmableInteraction();

    Mockito
      .when(cInteractionRepository.findFriendshipBetweenInteractors(15L, 12L))
      .thenReturn(interaction);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        cInteractionService.addPersonAsFriend(15L, 12L);
      }
    );
  }

  @Test
  public void addPersonAsFriendThrowsWhenThereIsAlreadyAnAcceptedInteractionExisting() {
    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setConfirmedAt(new Date(500L));

    Mockito
      .when(cInteractionRepository.findFriendshipBetweenInteractors(15L, 12L))
      .thenReturn(interaction);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        cInteractionService.addPersonAsFriend(15L, 12L);
      }
    );
  }

  @Test
  public void getPersonFriendsOfFriendsReturnsFriendsPersonDoesNotAlreadyHavePlusAppearancesInTheOtherLists() {
    Person interesting = new Person();
    interesting.setId(5L);

    Person friendA = new Person();
    friendA.setId(45L);
    Person friendB = new Person();
    friendB.setId(200L);
    Person friendC = new Person();
    friendC.setId(128L);

    ConfirmableInteraction friendship1 = new ConfirmableInteraction();
    friendship1.setInteractor(interesting);
    friendship1.setReceiverInteractor(friendA);
    ConfirmableInteraction friendship2 = new ConfirmableInteraction();
    friendship2.setInteractor(friendB);
    friendship2.setReceiverInteractor(interesting);
    ConfirmableInteraction friendship3 = new ConfirmableInteraction();
    friendship3.setInteractor(friendC);
    friendship3.setReceiverInteractor(interesting);

    Mockito
      .when(cInteractionRepository.findFriendsByInteractorId(5L))
      .thenReturn(List.of(friendship1, friendship2, friendship3));

    Person foo = new Person();
    foo.setId(900L);
    Person bar = new Person();
    bar.setId(850L);
    Person baz = new Person();
    baz.setId(875L);

    ConfirmableInteraction friendshipA1 = new ConfirmableInteraction();
    friendshipA1.setInteractor(friendA);
    friendshipA1.setReceiverInteractor(foo);
    ConfirmableInteraction friendshipA2 = new ConfirmableInteraction();
    friendshipA2.setInteractor(interesting);
    friendshipA2.setReceiverInteractor(friendA);
    ConfirmableInteraction friendshipA3 = new ConfirmableInteraction();
    friendshipA3.setInteractor(friendB);
    friendshipA3.setReceiverInteractor(friendA);
    ConfirmableInteraction friendshipA4 = new ConfirmableInteraction();
    friendshipA4.setInteractor(bar);
    friendshipA4.setReceiverInteractor(friendA);

    Mockito
      .when(cInteractionRepository.findFriendsByInteractorId(45L))
      .thenReturn(
        List.of(friendshipA1, friendshipA2, friendshipA3, friendshipA4)
      );

    ConfirmableInteraction friendshipB1 = new ConfirmableInteraction();
    friendshipB1.setInteractor(friendB);
    friendshipB1.setReceiverInteractor(friendC);
    ConfirmableInteraction friendshipB2 = new ConfirmableInteraction();
    friendshipB2.setInteractor(interesting);
    friendshipB2.setReceiverInteractor(friendB);
    ConfirmableInteraction friendshipB3 = new ConfirmableInteraction();
    friendshipB3.setInteractor(friendB);
    friendshipB3.setReceiverInteractor(bar);
    ConfirmableInteraction friendshipB4 = new ConfirmableInteraction();
    friendshipB4.setInteractor(friendB);
    friendshipB4.setReceiverInteractor(foo);

    Mockito
      .when(cInteractionRepository.findFriendsByInteractorId(200L))
      .thenReturn(
        List.of(friendshipB1, friendshipB2, friendshipB3, friendshipB4)
      );

    ConfirmableInteraction friendshipC1 = new ConfirmableInteraction();
    friendshipC1.setInteractor(friendB);
    friendshipC1.setReceiverInteractor(friendC);
    ConfirmableInteraction friendshipC2 = new ConfirmableInteraction();
    friendshipC2.setInteractor(bar);
    friendshipC2.setReceiverInteractor(friendC);
    ConfirmableInteraction friendshipC3 = new ConfirmableInteraction();
    friendshipC3.setInteractor(baz);
    friendshipC3.setReceiverInteractor(friendC);

    Mockito
      .when(cInteractionRepository.findFriendsByInteractorId(128L))
      .thenReturn(List.of(friendshipC1, friendshipC2, friendshipC3));

    Map<Person, Long> result = cInteractionService.getPersonFriendsOfFriends(
      5L
    );
    assertEquals(Map.of(bar, 3, foo, 2, baz, 1).toString(), result.toString()); // Seems like this piggy backs on the map types if not converted to String
  }
}
