package io.github.jmmedina00.adoolting.controller.group;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.group.NewEvent;
import io.github.jmmedina00.adoolting.entity.group.Event;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.group.EventService;
import java.util.Calendar;
import java.util.Date;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/event")
public class EventController {
  @Autowired
  private EventService eventService;

  private static int MINIMUM_HOURS = 2;

  @RequestMapping(method = RequestMethod.POST)
  public String createNewEvent(
    @ModelAttribute("newGroup") @Valid NewEvent newEvent,
    BindingResult result,
    RedirectAttributes attributes
  ) {
    if (result.hasErrors()) {
      attributes.addFlashAttribute(
        "org.springframework.validation.BindingResult.newGroup",
        result
      );
      attributes.addFlashAttribute("newGroup", newEvent);
      return "redirect:/group?event";
    }

    if (isDateBeforeThreshold(newEvent.getFinalizedDate())) {
      attributes.addFlashAttribute("newGroup", newEvent);
      return "redirect:/group?event&badtime";
    }

    Event event = eventService.createEvent(
      newEvent,
      AuthenticatedPerson.getPersonId()
    );
    return "redirect:/interaction/" + event.getId();
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{id}")
  public String updateEventInfo(
    @PathVariable("id") Long eventId,
    @ModelAttribute("form") @Valid NewEvent newEvent,
    BindingResult result,
    RedirectAttributes attributes
  )
    throws NotAuthorizedException {
    if (result.hasErrors()) {
      attributes.addFlashAttribute(
        "org.springframework.validation.BindingResult.form",
        result
      );
      attributes.addFlashAttribute("form", newEvent);
      return "redirect:/group/" + eventId;
    }

    if (isDateBeforeThreshold(newEvent.getFinalizedDate())) {
      attributes.addFlashAttribute("newGroup", newEvent);
      return "redirect:/group?event&badtime";
    }

    eventService.updateEvent(
      eventId,
      AuthenticatedPerson.getPersonId(),
      newEvent
    );

    return "redirect:/interaction/" + eventId;
  }

  private boolean isDateBeforeThreshold(Date date) {
    Calendar calendarThreshold = Calendar.getInstance();
    calendarThreshold.add(Calendar.HOUR_OF_DAY, MINIMUM_HOURS);
    return calendarThreshold.getTime().after(date);
  }
}
