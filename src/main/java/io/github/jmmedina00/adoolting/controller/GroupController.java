package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.group.NewEvent;
import io.github.jmmedina00.adoolting.dto.group.NewGroup;
import io.github.jmmedina00.adoolting.entity.group.Event;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.service.group.EventService;
import io.github.jmmedina00.adoolting.service.group.PeopleGroupService;
import java.util.Calendar;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/group")
public class GroupController {
  @Autowired
  private PeopleGroupService groupService;

  @Autowired
  private EventService eventService;

  private static int MINIMUM_HOURS = 2;

  @RequestMapping(method = RequestMethod.GET)
  public String getNewGroupForm(
    @RequestParam(required = false, name = "event") String eventStr,
    Model model
  ) {
    if (!model.containsAttribute("newGroup")) {
      model.addAttribute(
        "newGroup",
        eventStr == null ? new NewGroup() : new NewEvent()
      );
    }

    return "form/group";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String createNewGroup(
    @ModelAttribute("newGroup") @Valid NewGroup newGroup,
    BindingResult result,
    RedirectAttributes attributes
  ) {
    if (result.hasErrors()) {
      attributes.addFlashAttribute(
        "org.springframework.validation.BindingResult.newGroup",
        result
      );
      attributes.addFlashAttribute("newGroup", newGroup);
      return "redirect:/group";
    }

    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    PeopleGroup group = groupService.createGroup(newGroup, authenticatedPerson);
    return "redirect:/interaction/" + group.getId();
  }

  @RequestMapping(method = RequestMethod.POST, value = "/event")
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

    Calendar calendarThreshold = Calendar.getInstance();
    calendarThreshold.add(Calendar.HOUR_OF_DAY, MINIMUM_HOURS);

    if (calendarThreshold.getTime().after(newEvent.getFinalizedDate())) {
      attributes.addFlashAttribute("newGroup", newEvent);
      return "redirect:/group?event&badtime";
    }

    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    Event event = eventService.createEvent(newEvent, authenticatedPerson);
    return "redirect:/interaction/" + event.getId();
  }
}
