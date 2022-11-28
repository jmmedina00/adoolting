package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/interaction")
public class InteractionController {
  @Autowired
  private InteractionService interactionService;

  @RequestMapping(method = RequestMethod.GET, value = "/{id}")
  public String getInteraction(
    @PathVariable("id") String interactionIdStr,
    Model model
  ) {
    Long interactionId;
    Interaction interaction;
    try {
      interactionId = Long.parseLong(interactionIdStr);
      interaction = interactionService.getInteraction(interactionId);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    model.addAttribute("interaction", interaction);

    return "interaction";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{id}/delete")
  public String deleteInteraction(@PathVariable("id") String interactionIdStr) {
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

    try {
      interactionService.deleteInteraction(interactionId, authenticatedPerson);
    } catch (NotAuthorizedException e) {
      return "redirect:/home?notfound";
    }

    return "redirect:/profile/" + authenticatedPerson.getId() + "?success";
  }
}
