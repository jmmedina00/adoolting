package io.github.jmmedina00.adoolting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.Event;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.InteractionRepository;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.NotificationService;
import io.github.jmmedina00.adoolting.service.person.NotifiedInteractorService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
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

  @Test
  public void getAppropriateInteractorListForPersonReturnsPersonPlusPageWhenItIsACommentPostedByPageAndPersonManagesIt() {
    Person person = new Person();
    Page page = new Page();
    page.setId(20L);
    Comment comment = new Comment();
    Event event = new Event();
    event.setInteractor(page);
    comment.setReceiverInteraction(event);

    Mockito.when(interactorService.getInteractor(4L)).thenReturn(person);
    Mockito
      .when(interactionRepository.findActiveInteraction(120L))
      .thenReturn(Optional.of(comment));
    Mockito.when(pageService.isPageManagedByPerson(20L, 4L)).thenReturn(true);

    List<Interactor> possibleInteractors = interactionService.getAppropriateInteractorListForPerson(
      4L,
      120L
    );
    assertEquals(List.of(person, page), possibleInteractors);
  }

  @Test
  public void getAppropriateInteractorListForPersonReturnsPersonWhenItIsACommentPostedByPageAndPersonDoesNotManageIt() {
    Person person = new Person();
    Page page = new Page();
    page.setId(20L);
    Comment comment = new Comment();
    Event event = new Event();
    event.setInteractor(page);
    comment.setReceiverInteraction(event);

    Mockito.when(interactorService.getInteractor(4L)).thenReturn(person);
    Mockito
      .when(interactionRepository.findActiveInteraction(120L))
      .thenReturn(Optional.of(comment));
    Mockito.when(pageService.isPageManagedByPerson(20L, 4L)).thenReturn(false);

    List<Interactor> possibleInteractors = interactionService.getAppropriateInteractorListForPerson(
      4L,
      120L
    );
    assertEquals(List.of(person), possibleInteractors);
  }

  @Test
  public void getAppropriateInteractorListForPersonReturnsPersonPlusPageWhenInteractionReceivedByPageManagedByPerson() {
    Person person = new Person();
    Page foo = new Page();
    foo.setId(20L);
    Page bar = new Page();
    bar.setId(21L);

    Post post = new Post();
    post.setInteractor(foo);
    post.setReceiverInteractor(bar);

    Mockito.when(interactorService.getInteractor(4L)).thenReturn(person);
    Mockito
      .when(interactionRepository.findActiveInteraction(120L))
      .thenReturn(Optional.of(post));
    Mockito.when(pageService.isPageManagedByPerson(21L, 4L)).thenReturn(true);

    List<Interactor> possibleInteractors = interactionService.getAppropriateInteractorListForPerson(
      4L,
      120L
    );
    assertEquals(List.of(person, bar), possibleInteractors);
    verify(interactorService, never())
      .getRepresentableInteractorsByPerson(4L, 20L);
  }

  @Test
  public void getAppropriateInteractorListForPersonDefaultsToRepresentableInteractorsInFrontOfFirstInteractor() {
    Person person = new Person();
    Page foo = new Page();
    foo.setId(20L);
    Page bar = new Page();
    bar.setId(21L);

    Post post = new Post();
    post.setInteractor(foo);
    post.setReceiverInteractor(bar);

    Mockito.when(interactorService.getInteractor(4L)).thenReturn(person);
    Mockito
      .when(interactionRepository.findActiveInteraction(120L))
      .thenReturn(Optional.of(post));
    Mockito.when(pageService.isPageManagedByPerson(21L, 4L)).thenReturn(false);

    interactionService.getAppropriateInteractorListForPerson(4L, 120L);
    verify(interactorService, times(1))
      .getRepresentableInteractorsByPerson(4L, 20L);
  }

  @Test
  public void getAppropriateInteractorListForPersonOnlyAdmitsPersonAsFirstParameter() {
    Page page = new Page();
    Mockito.when(interactorService.getInteractor(4L)).thenReturn(page);

    assertThrows(
      ClassCastException.class,
      () -> {
        interactionService.getAppropriateInteractorListForPerson(4L, 120L);
      }
    );
  }
}
