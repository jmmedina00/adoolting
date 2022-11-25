package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.InteractionConfirmation;
import io.github.jmmedina00.adoolting.entity.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/network")
public class ConfirmableInteractionController {
  @Autowired
  private ConfirmableInteractionService cInteractionService;

  @RequestMapping(method = RequestMethod.POST, value = "/{interactionId}")
  public String decideInteractionResult(
    @Valid @RequestBody InteractionConfirmation confirmation
  ) {
    return "";
  }

  @RequestMapping(method = RequestMethod.GET)
  public String getPendingConfirmableInteractions(Model model) {
    Authentication authentication = SecurityContextHolder
      .getContext()
      .getAuthentication();

    if (authentication instanceof AnonymousAuthenticationToken) {
      return "redirect:/";
    }

    Person person = ((PersonDetails) authentication.getPrincipal()).getPerson();
    model.addAttribute(
      "interactions",
      cInteractionService.getPendingInteractionsForPerson(person)
    );

    return "network";
  }
}
