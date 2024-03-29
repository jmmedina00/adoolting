package io.github.jmmedina00.adoolting.controller.group;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.group.NewGroup;
import io.github.jmmedina00.adoolting.dto.util.SecureDeletion;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.group.JoinRequestService;
import io.github.jmmedina00.adoolting.service.group.PeopleGroupService;
import java.util.Optional;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/group/{id}")
public class GroupOperationsController {
  @Autowired
  private PeopleGroupService groupService;

  @Autowired
  private JoinRequestService joinRequestService;

  private static final Logger logger = LoggerFactory.getLogger(
    GroupOperationsController.class
  );

  @RequestMapping(method = RequestMethod.GET)
  public String getGroupManagementForm(
    @PathVariable("id") Long groupId,
    Model model
  )
    throws NotAuthorizedException {
    model.addAttribute(
      "group",
      groupService.getGroupManagedByPerson(
        groupId,
        AuthenticatedPerson.getPersonId()
      )
    );
    model.addAttribute(
      "cInteractions",
      joinRequestService.getExistingForGroup(groupId)
    );
    if (!model.containsAttribute("form")) {
      model.addAttribute("form", groupService.getGroupForm(groupId));
    }

    logger.debug(
      "Current group form class: {}",
      Optional
        .ofNullable(model.getAttribute("form"))
        .orElse(new NewGroup())
        .getClass()
    );
    return "group/manage";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String updateGroupInfo(
    @PathVariable("id") Long groupId,
    @ModelAttribute("form") @Valid NewGroup newGroup
  )
    throws NotAuthorizedException {
    groupService.updateGroup(
      groupId,
      AuthenticatedPerson.getPersonId(),
      newGroup
    );

    return "redirect:/interaction/" + groupId;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/delete")
  public String showDeleteForm(@PathVariable("id") Long groupId, Model model)
    throws NotAuthorizedException {
    model.addAttribute(
      "group",
      groupService.getGroupManagedByPerson(
        groupId,
        AuthenticatedPerson.getPersonId()
      )
    );
    if (!model.containsAttribute("confirm")) {
      model.addAttribute("confirm", new SecureDeletion());
    }
    return "group/delete";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/delete")
  public String deleteGroup(
    @PathVariable("id") Long groupId,
    @ModelAttribute("confirm") @Valid SecureDeletion confirmation
  )
    throws Exception {
    groupService.deleteGroup(
      groupId,
      AuthenticatedPerson.getPersonId(),
      confirmation
    );
    return "redirect:/profile";
  }
}
