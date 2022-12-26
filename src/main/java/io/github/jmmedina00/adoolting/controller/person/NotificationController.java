package io.github.jmmedina00.adoolting.controller.person;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.entity.person.Notification;
import io.github.jmmedina00.adoolting.service.person.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/notification")
public class NotificationController {
  @Autowired
  private NotificationService notificationService;

  @RequestMapping(method = RequestMethod.GET)
  public String getNotifications(
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
    return "person/notification";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{id}")
  public String goToNotificationInteraction(
    @PathVariable("id") Long notificationId
  ) {
    Notification notification = notificationService.markNotificationAsRead(
      notificationId,
      AuthenticatedPerson.getPersonId()
    );
    return "redirect:/interaction/" + notification.getInteraction().getId();
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{id}/delete")
  public String deleteNotification(@PathVariable("id") Long notificationId) {
    notificationService.deleteNotification(
      notificationId,
      AuthenticatedPerson.getPersonId()
    );
    return "redirect:/notification";
  }
}
