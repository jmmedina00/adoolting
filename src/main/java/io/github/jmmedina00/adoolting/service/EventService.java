package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.dto.NewEvent;
import io.github.jmmedina00.adoolting.entity.Event;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {
  @Autowired
  private EventRepository eventRepository;

  public Event createEvent(NewEvent newEvent, Interactor interactor) {
    Event event = new Event();
    event.setName(newEvent.getName());
    event.setDescription(newEvent.getDescription());
    event.setInteractor(interactor);
    event.setLocation(newEvent.getLocation());
    event.setHappeningAt(newEvent.getFinalizedDate());

    return eventRepository.save(event);
  }
}
