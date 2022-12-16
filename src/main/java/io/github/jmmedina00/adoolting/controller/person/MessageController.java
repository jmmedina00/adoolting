package io.github.jmmedina00.adoolting.controller.person;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.person.NewMessage;
import io.github.jmmedina00.adoolting.service.cache.PersonLatestMessagesService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import io.github.jmmedina00.adoolting.service.person.PrivateMessageService;
import java.util.Objects;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
  private PersonLatestMessagesService latestMessagesService;

  @Autowired
  private PersonService personService;

  @RequestMapping(method = RequestMethod.GET)
  public String getRecentMessagesList(Model model) {
    model.addAttribute(
      "messages",
      latestMessagesService.getLatestMessagesForPerson(
        AuthenticatedPerson.getPersonId()
      )
    );
    return "message/list";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{personId}")
  public String getMessagesWithPerson(
    @PathVariable("personId") Long personId,
    @PageableDefault(value = 10, page = 0) Pageable pageable,
    Model model
  ) {
    Long authenticatedPersonId = AuthenticatedPerson.getPersonId();
    if (Objects.equals(personId, authenticatedPersonId)) {
      return "redirect:/message";
    }

    model.addAttribute("person", personService.getPerson(personId));
    model.addAttribute(
      "messages",
      messageService.getMessagesBetweenPersons(
        authenticatedPersonId,
        personId,
        pageable
      )
    );
    model.addAttribute("newMessage", new NewMessage());
    return "message/conversation";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{personId}")
  public String sendMessageToPerson(
    @PathVariable("personId") Long personId,
    @ModelAttribute("newMessage") @Valid NewMessage newMessage
  ) {
    Long authenticatedPersonId = AuthenticatedPerson.getPersonId();

    if (Objects.equals(personId, authenticatedPersonId)) {
      return "redirect:/message";
    }

    messageService.sendMessageToPerson(
      authenticatedPersonId,
      personId,
      newMessage
    );
    return "redirect:/message/" + personId + "?success";
  }
}
