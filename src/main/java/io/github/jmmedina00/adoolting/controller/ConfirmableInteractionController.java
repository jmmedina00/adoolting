package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.InteractionConfirmation;
import io.github.jmmedina00.adoolting.dto.NewConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.exception.InvalidDTOException;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    try {
      Long interactionId = Long.parseLong(interactionIdStr);
      ConfirmableInteraction interaction = cInteractionService.decideInteractionResult(
        interactionId,
        AuthenticatedPerson.getPersonId(),
        confirmation.getIsAccepted()
      );

      return (!confirmation.getGoToProfile())
        ? "redirect:/network"
        : "redirect:/profile/" +
        interaction.getInteractor().getId() +
        "?success";
    } catch (NotAuthorizedException e) {
      return "redirect:/home?notfound";
    }
  }

  @RequestMapping(method = RequestMethod.POST)
  public String addFriend(
    @ModelAttribute(
      "cInteraction"
    ) @Valid NewConfirmableInteraction nConfirmableInteraction
  ) {
    try {
      cInteractionService.addPersonAsFriend(
        AuthenticatedPerson.getPersonId(),
        nConfirmableInteraction.getPersonId()
      );
    } catch (InvalidDTOException e) {
      return "redirect:/home?notfound";
    }

    return "redirect:/profile/" + nConfirmableInteraction.getPersonId();
  }

  @RequestMapping(method = RequestMethod.GET)
  public String getPendingConfirmableInteractions(Model model) {
    model.addAttribute(
      "interactions",
      cInteractionService.getPendingInteractionsForPerson(
        AuthenticatedPerson.getPersonId()
      )
    );
    return "network";
  }
}
