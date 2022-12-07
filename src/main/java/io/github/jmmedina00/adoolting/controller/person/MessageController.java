package io.github.jmmedina00.adoolting.controller.person;

import io.github.jmmedina00.adoolting.dto.person.NewMessage;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import io.github.jmmedina00.adoolting.service.person.PrivateMessageService;
import java.util.Objects;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/message")
public class MessageController {
  @Autowired
  private PrivateMessageService messageService;

  @Autowired
  private PersonService personService;

  @RequestMapping(method = RequestMethod.GET)
  public String getRecentMessagesList(Model model) {
    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    model.addAttribute(
      "messages",
      messageService.getLatestMessagesForPerson(authenticatedPerson.getId())
    );
    return "message/list";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{personId}")
  public String getMessagesWithPerson(
    @PathVariable("personId") String personIdStr,
    Model model
  ) {
    Long personId;

    try {
      personId = Long.parseLong(personIdStr);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    Person person = personService.getPerson(personId);
    if (Objects.equals(person.getId(), authenticatedPerson.getId())) {
      return "redirect:/message";
    }

    model.addAttribute("person", person);
    model.addAttribute(
      "messages",
      messageService.getMessagesBetweenPersons(
        authenticatedPerson.getId(),
        person.getId()
      )
    );
    model.addAttribute("newMessage", new NewMessage());
    return "message/conversation";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{personId}")
  public String sendMessageToPerson(
    @PathVariable("personId") String personIdStr,
    @ModelAttribute("newMessage") @Valid NewMessage newMessage,
    BindingResult result
  ) {
    Long personId;

    try {
      personId = Long.parseLong(personIdStr);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    if (result.hasErrors()) {
      return "redirect:/message/" + personId + "?error";
    }

    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    if (Objects.equals(personId, authenticatedPerson.getId())) {
      return "redirect:/message";
    }

    messageService.sendMessageToPerson(
      authenticatedPerson.getId(),
      personId,
      newMessage
    );
    return "redirect:/message/" + personId + "?success";
  }
}
