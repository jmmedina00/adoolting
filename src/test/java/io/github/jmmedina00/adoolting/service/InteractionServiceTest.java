package io.github.jmmedina00.adoolting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.InteractionRepository;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.NotificationService;
import io.github.jmmedina00.adoolting.service.person.NotifiedInteractorService;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class InteractionServiceTest {
  @MockBean
  private InteractionRepository interactionRepository;

  @MockBean
  private NotificationService notificationService; // ALL INTERACTIONS GO THROUGH HERE OwO

  @MockBean
  private NotifiedInteractorService notifiedInteractorService;

  @MockBean
  private InteractorService interactorService;

  @MockBean
  private PageService pageService;

  @Autowired
  private InteractionService interactionService;

  @Test
  public void saveInteractionSavesToRepositoryAndNotifiesPersonsAccordingToNotifiedInteractorService() {
    Post post = new Post();
    Person foo = new Person();
    Person bar = new Person();
    Person baz = new Person();

    HashMap<Person, Integer> interested = new HashMap<>();
    interested.put(foo, 1);
    interested.put(bar, 2);
    interested.put(baz, 3);

    Mockito
      .when(
        notifiedInteractorService.getInteractorsInterestedInInteraction(post)
      )
      .thenReturn(interested);
    Mockito
      .when(interactionRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Interaction saved = interactionService.saveInteraction(post);
    assertEquals(post, saved);

    verify(notificationService, times(1)).createNotifications(saved, foo, 1);
    verify(notificationService, times(1)).createNotifications(saved, bar, 2);
    verify(notificationService, times(1)).createNotifications(saved, baz, 3);
  }

  @Test
  public void getInteractionGetsInteractionFromRepository() {
    Post post = new Post();
    Mockito
      .when(interactionRepository.findActiveInteraction(4L))
      .thenReturn(Optional.of(post));

    Interaction interaction = interactionService.getInteraction(4L);
    assertEquals(post, interaction);
  }

  @Test
  public void getInteractionThrowsWhenInteractionIsNotFound() {
    Mockito
      .when(interactionRepository.findActiveInteraction(4L))
      .thenReturn(Optional.empty());
    assertThrows(
      NoSuchElementException.class,
      () -> {
        interactionService.getInteraction(4L);
      }
    );
  }

  @Test
  public void getInteractionsFromInteractorQueriesRepositoryWithIdWrappedInAList() {
    PageRequest request = PageRequest.of(3, 12);
    interactionService.getInteractionsFromInteractor(5L, request);

    verify(interactionRepository, times(1))
      .findInteractionsByInteractorIds(List.of(5L), request);
  }

  @Test
  public void deleteInteractionSetsInteractionDeletedAtToNewDate()
    throws NotAuthorizedException {
    Post post = new Post();

    Mockito
      .when(interactionRepository.findDeletableInteractionForInteractor(5L, 2L))
      .thenReturn(Optional.of(post));
    interactionService.deleteInteraction(5L, 2L);

    assertNotNull(post.getDeletedAt());
    verify(interactionRepository, times(1)).save(post);
  }

  @Test
  public void deleteInteractionThrowsIfInteractionCannotBeFound() {
    Mockito
      .when(interactionRepository.findDeletableInteractionForInteractor(5L, 2L))
      .thenReturn(Optional.empty());

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        interactionService.deleteInteraction(5L, 2L);
      }
    );
  }
}
