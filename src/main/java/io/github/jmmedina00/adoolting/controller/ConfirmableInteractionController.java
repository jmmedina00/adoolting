package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.InteractionConfirmation;
import io.github.jmmedina00.adoolting.dto.NewConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.exception.InvalidDTOException;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/network")
public class ConfirmableInteractionController {
  @Autowired
  private ConfirmableInteractionService cInteractionService;

  @RequestMapping(
    method = RequestMethod.POST,
    value = "/{interactionId}",
    consumes = "application/x-www-form-urlencoded"
  )
  public String decideInteractionResult(
    @Valid InteractionConfirmation confirmation,
    @PathVariable("interactionId") String interactionIdStr
  ) {
    Long interactionId;

    try {
      interactionId = Long.parseLong(interactionIdStr);
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

    ConfirmableInteraction interaction;

    try {
      interaction =
        cInteractionService.decideInteractionResult(
          interactionId,
          authenticatedPerson,
          confirmation.getIsAccepted()
        );
    } catch (NotAuthorizedException e) {
      return "redirect:/home?notfound";
    }

    return (!confirmation.getGoToProfile())
      ? "redirect:/network"
      : "redirect:/profile/" + interaction.getInteractor().getId() + "?success";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String addFriend(
    @ModelAttribute(
      "cInteraction"
    ) @Valid NewConfirmableInteraction nConfirmableInteraction
  ) {
    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    try {
      cInteractionService.addPersonAsFriend(
        authenticatedPerson,
        nConfirmableInteraction.getPersonId()
      );
    } catch (InvalidDTOException e) {
      return "redirect:/home?notfound";
    }

    return "redirect:/profile/" + nConfirmableInteraction.getPersonId();
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
