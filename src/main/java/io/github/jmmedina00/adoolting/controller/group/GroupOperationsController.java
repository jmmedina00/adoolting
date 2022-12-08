package io.github.jmmedina00.adoolting.controller.group;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.group.NewGroup;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.group.JoinRequestService;
import io.github.jmmedina00.adoolting.service.group.PeopleGroupService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/group/{id}")
public class GroupOperationsController {
  @Autowired
  private PeopleGroupService groupService;

  @Autowired
  private JoinRequestService joinRequestService;

  @RequestMapping(method = RequestMethod.GET)
  public String getGroupManagementForm(
    @PathVariable("id") Long groupId,
    Model model
  )
    throws NotAuthorizedException {
    PeopleGroup group = groupService.getGroup(groupId);

    if (
      !groupService.isGroupManagedByPerson(
        groupId,
        AuthenticatedPerson.getPersonId()
      )
    ) {
      throw new NotAuthorizedException();
    }

    model.addAttribute("group", group);
    model.addAttribute(
      "cInteractions",
      joinRequestService.getExistingForGroup(groupId)
    );
    if (!model.containsAttribute("form")) {
      model.addAttribute("form", groupService.getGroupForm(groupId));
    }
    return "group-manage";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String updateGroupInfo(
    @PathVariable("id") Long groupId,
    @ModelAttribute("form") @Valid NewGroup newGroup,
    BindingResult result,
    RedirectAttributes attributes
  )
    throws NotAuthorizedException {
    if (result.hasErrors()) {
      attributes.addFlashAttribute(
        "org.springframework.validation.BindingResult.form",
        result
      );
      attributes.addFlashAttribute("form", newGroup);
      return "redirect:/group/" + groupId;
    }

    groupService.updateGroup(
      groupId,
      AuthenticatedPerson.getPersonId(),
      newGroup
    );

    return "redirect:/interaction/" + groupId;
  }

  @RequestMapping(method = RequestMethod.POST, value = "/join")
  public String requestToJoinGroup(@PathVariable("id") Long groupId)
    throws NotAuthorizedException {
    joinRequestService.joinGroup(AuthenticatedPerson.getPersonId(), groupId);
    return "redirect:/interaction/" + groupId;
  }

  @RequestMapping(method = RequestMethod.POST, value = "/invite/{personId}")
  public String invitePersonToGroup(
    @PathVariable("id") Long groupId,
    @PathVariable("personId") Long personId
  )
    throws NotAuthorizedException {
    joinRequestService.inviteToGroup(
      AuthenticatedPerson.getPersonId(),
      personId,
      groupId
    );

    return "redirect:/profile/" + personId;
  }
}
