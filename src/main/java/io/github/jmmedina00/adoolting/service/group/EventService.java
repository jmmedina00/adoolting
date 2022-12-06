package io.github.jmmedina00.adoolting.service.group;

import io.github.jmmedina00.adoolting.dto.group.NewEvent;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.Event;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.group.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {
  @Autowired
  private EventRepository eventRepository;

  @Autowired
  private PeopleGroupService groupService;

  public Event createEvent(NewEvent newEvent, Interactor interactor) {
    Event event = new Event();
    event.setName(newEvent.getName());
    event.setDescription(newEvent.getDescription());
    event.setInteractor(interactor);
    event.setLocation(newEvent.getLocation());
    event.setHappeningAt(newEvent.getFinalizedDate());

    return eventRepository.save(event);
  }

  public Event updateEvent(Long eventId, Long personId, NewEvent newEvent)
    throws NotAuthorizedException {
    if (!groupService.isGroupManagedByPerson(eventId, personId)) {
      throw new NotAuthorizedException();
    }

    Event event = eventRepository.findById(eventId).orElseThrow();
    event.setName(newEvent.getName());
    event.setDescription(newEvent.getDescription());
    event.setLocation(newEvent.getLocation());
    event.setHappeningAt(newEvent.getFinalizedDate());
    return eventRepository.save(event);
  }
}
