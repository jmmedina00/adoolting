package io.github.jmmedina00.adoolting.service.group;

import io.github.jmmedina00.adoolting.dto.group.NewEvent;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.Event;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.group.EventRepository;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {
  @Autowired
  private EventRepository eventRepository;

  @Autowired
  private PeopleGroupService groupService;

  @Autowired
  private InteractorService interactorService;

  @Autowired
  private InteractionService interactionService;

  @Autowired
  private PageService pageService;

  private static final Logger logger = LoggerFactory.getLogger(
    EventService.class
  );

  public Event createEvent(NewEvent newEvent, Long personId)
    throws NotAuthorizedException {
    Long interactorId = newEvent.getCreateAs();

    if (
      !(
        Objects.equals(personId, interactorId) ||
        pageService.isPageManagedByPerson(interactorId, personId)
      )
    ) {
      throw new NotAuthorizedException();
    }

    Interactor interactor = interactorService.getInteractor(interactorId);
    Event event = new Event();
    event.setName(newEvent.getName());
    event.setDescription(newEvent.getDescription());
    event.setAccessLevel(newEvent.getAccessLevel());
    event.setInteractor(interactor);
    event.setLocation(newEvent.getLocation());
    event.setHappeningAt(newEvent.getFinalizedDate());

    Event saved = (Event) interactionService.saveInteraction(event);

    if (Objects.equals(personId, interactorId)) {
      logger.info(
        "New event (id={}) created by person {}.",
        saved.getId(),
        personId
      );
    } else {
      logger.info(
        "New event (id={}) created by person {} for interactor {}.",
        saved.getId(),
        personId,
        interactorId
      );
    }

    return saved;
  }

  public Event updateEvent(Long eventId, Long personId, NewEvent newEvent)
    throws NotAuthorizedException {
    if (!groupService.isGroupManagedByPerson(eventId, personId)) {
      throw new NotAuthorizedException();
    }

    Event event = eventRepository.findById(eventId).orElseThrow();
    event.setName(newEvent.getName());
    event.setDescription(newEvent.getDescription());
    event.setAccessLevel(newEvent.getAccessLevel());
    event.setLocation(newEvent.getLocation());
    event.setHappeningAt(newEvent.getFinalizedDate());

    logger.info("Event {} has been updated by person {}", eventId, personId);

    return eventRepository.save(event); // No need to go through default notif flow
  }
}
