package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.InteractionConfirmation;
import io.github.jmmedina00.adoolting.dto.NewConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.person.Notification;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import io.github.jmmedina00.adoolting.service.person.NotificationService;
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
@RequestMapping("/network")
public class ConfirmableInteractionController {
  @Autowired
  private ConfirmableInteractionService cInteractionService;

  @Autowired
  private NotificationService notificationService;

  @RequestMapping(
    method = RequestMethod.POST,
    value = "/{interactionId}",
    consumes = "application/x-www-form-urlencoded"
  )
  public String decideInteractionResult(
    @Valid InteractionConfirmation confirmation,
    @PathVariable("interactionId") Long interactionId
  )
    throws NotAuthorizedException {
    ConfirmableInteraction interaction = cInteractionService.decideInteractionResult(
      interactionId,
      AuthenticatedPerson.getPersonId(),
      confirmation.getIsAccepted()
    );

    return (!confirmation.getGoToProfile())
      ? "redirect:/network"
      : "redirect:/profile/" + interaction.getInteractor().getId() + "?success";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String addFriend(
    @ModelAttribute(
      "cInteraction"
    ) @Valid NewConfirmableInteraction nConfirmableInteraction
  )
    throws NotAuthorizedException {
    cInteractionService.addPersonAsFriend(
      AuthenticatedPerson.getPersonId(),
      nConfirmableInteraction.getPersonId()
    );
    return "redirect:/profile/" + nConfirmableInteraction.getPersonId();
  }

  @RequestMapping(method = RequestMethod.GET)
  public String getPendingConfirmableInteractions(
    Model model,
    @PageableDefault(value = 10, page = 0) Pageable pageable
  ) {
    model.addAttribute(
      "notifications",
      notificationService.getNotificationsForPerson(
        AuthenticatedPerson.getPersonId(),
        pageable
      )
    );
    return "network";
  }

  @RequestMapping(
    method = RequestMethod.POST,
    value = "/delete/{notificationId}"
  )
  public String deleteNotification(
    @PathVariable("notificationId") Long notificationId
  ) {
    notificationService.deleteNotification(
      notificationId,
      AuthenticatedPerson.getPersonId()
    );
    return "redirect:/network";
  }

  @RequestMapping(
    method = RequestMethod.GET,
    value = "/passthrough/{notificationId}"
  )
  public String goToNotificationInteraction(
    @PathVariable("notificationId") Long notificationId
  ) {
    Notification notification = notificationService.markNotificationAsRead(
      notificationId,
      AuthenticatedPerson.getPersonId()
    );
    return "redirect:/interaction/" + notification.getInteraction().getId();
  }
}
