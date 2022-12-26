package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.InteractionConfirmation;
import io.github.jmmedina00.adoolting.dto.NewConfirmableInteraction;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import io.github.jmmedina00.adoolting.service.group.JoinRequestService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/confirmable")
public class ConfirmableInteractionController {
  @Autowired
  private ConfirmableInteractionService cInteractionService;

  @Autowired
  private JoinRequestService joinRequestService;

  @RequestMapping(method = RequestMethod.POST)
  public String addFriend(
    @ModelAttribute(
      "cInteraction"
    ) @Valid NewConfirmableInteraction nConfirmableInteraction,
    HttpServletRequest request
  )
    throws NotAuthorizedException {
    cInteractionService.addPersonAsFriend(
      AuthenticatedPerson.getPersonId(),
      nConfirmableInteraction.getPersonId()
    );
    return redirectToPreviousPage(request);
  }

  @RequestMapping(
    method = RequestMethod.POST,
    value = "/{interactionId}",
    consumes = "application/x-www-form-urlencoded"
  )
  public String decideInteractionResult(
    @Valid InteractionConfirmation confirmation,
    @PathVariable("interactionId") Long interactionId,
    HttpServletRequest request
  )
    throws NotAuthorizedException {
    cInteractionService.decideInteractionResult(
      interactionId,
      AuthenticatedPerson.getPersonId(),
      confirmation.getIsAccepted()
    );

    return redirectToPreviousPage(request);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/group/{id}/join")
  public String requestToJoinGroup(
    @PathVariable("id") Long groupId,
    HttpServletRequest request
  )
    throws NotAuthorizedException {
    joinRequestService.joinGroup(AuthenticatedPerson.getPersonId(), groupId);
    return "redirect:/interaction/" + groupId;
  }

  @RequestMapping(method = RequestMethod.POST, value = "/group/{id}/{personId}")
  public String invitePersonToGroup(
    @PathVariable("id") Long groupId,
    @PathVariable("personId") Long personId,
    HttpServletRequest request
  )
    throws NotAuthorizedException {
    joinRequestService.inviteToGroup(
      AuthenticatedPerson.getPersonId(),
      personId,
      groupId
    );

    return redirectToPreviousPage(request);
  }

  private String redirectToPreviousPage(HttpServletRequest request) {
    String requestingPath = request
      .getHeader("Referer")
      .replaceFirst("\\?.+$", "");

    return "redirect:" + requestingPath;
  }
}
