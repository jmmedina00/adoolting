package io.github.jmmedina00.adoolting.service.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.dto.group.NewEvent;
import io.github.jmmedina00.adoolting.entity.enums.GroupAccessLevel;
import io.github.jmmedina00.adoolting.entity.group.Event;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.group.EventRepository;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class EventServiceTest {
  @MockBean
  private EventRepository eventRepository;

  @MockBean
  private PeopleGroupService groupService;

  @MockBean
  private InteractorService interactorService;

  @MockBean
  private InteractionService interactionService;

  @MockBean
  private PageService pageService;

  @Autowired
  private EventService eventService;

  // TODO: test when permission check fails

  NewEvent newEvent = new NewEvent();

  @BeforeEach
  public void prepareDto() {
    newEvent.setName("My event");
    newEvent.setDescription("This is an event");
    newEvent.setAccessLevel(GroupAccessLevel.OPEN);
    newEvent.setLocation("Somewhere");
    newEvent.setCreateAs(1L);
    newEvent.setDate(new Date(1899331200000L)); // 2030/03/10
    newEvent.setTime(new Date(44580000L)); // 12:23
    newEvent.setOffsetFromUTC(30);
  }

  @Test
  public void createEventPopulatesEventWithSpecifiedDetailsAndSavesThroughInteractionService()
    throws NotAuthorizedException {
    Person person = new Person();
    Mockito.when(interactorService.getInteractor(1L)).thenReturn(person);
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    Event event = eventService.createEvent(newEvent, 1L);

    assertEquals(newEvent.getName(), event.getName());
    assertEquals(newEvent.getDescription(), event.getDescription());
    assertEquals(newEvent.getAccessLevel(), event.getAccessLevel());
    assertEquals(newEvent.getLocation(), event.getLocation());
    assertEquals(person, event.getInteractor());
    assertEquals(newEvent.getFinalizedDate(), event.getHappeningAt());

    verify(interactionService, times(1)).saveInteraction(event);
    verify(eventRepository, never()).save(event);
  }

  @Test
  public void updateEventSavesEventToEventRepository()
    throws NotAuthorizedException {
    Event event = new Event();
    event.setName("null");

    Mockito.when(groupService.isGroupManagedByPerson(1L, 10L)).thenReturn(true);
    Mockito.when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
    Mockito
      .when(eventRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Event updated = eventService.updateEvent(1L, 10L, newEvent);

    assertEquals(newEvent.getName(), updated.getName());
    assertEquals(newEvent.getDescription(), updated.getDescription());
    assertEquals(newEvent.getAccessLevel(), updated.getAccessLevel());
    assertEquals(newEvent.getLocation(), updated.getLocation());
    assertEquals(newEvent.getFinalizedDate(), updated.getHappeningAt());

    verify(interactionService, never()).saveInteraction(event);
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  public void updateEventThrowsIfNotManagedByAttemptingPerson() {
    Mockito
      .when(groupService.isGroupManagedByPerson(1L, 24L))
      .thenReturn(false);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        eventService.updateEvent(1L, 24L, newEvent);
      }
    );
  }
}
