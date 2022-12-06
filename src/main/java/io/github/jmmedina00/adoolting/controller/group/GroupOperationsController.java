package io.github.jmmedina00.adoolting.controller.group;

import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.service.group.JoinRequestService;
import io.github.jmmedina00.adoolting.service.group.PeopleGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

  @RequestMapping(method = RequestMethod.GET)
  public String getGroupManagementForm(
    @PathVariable("id") String groupIdStr,
    Model model
  ) {
    Long groupId;
    PeopleGroup group;

    try {
      groupId = Long.parseLong(groupIdStr);
      group = groupService.getGroup(groupId);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    model.addAttribute("group", group);
    model.addAttribute(
      "cInteractions",
      joinRequestService.getExistingForGroup(groupId)
    );
    model.addAttribute("form", groupService.getGroupForm(groupId));
    return "group-manage";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/join")
  public String requestToJoinGroup(@PathVariable("id") String groupIdStr) {
    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    Long groupId;

    try {
      groupId = Long.parseLong(groupIdStr);
      joinRequestService.joinGroup(authenticatedPerson.getId(), groupId);
      return "redirect:/interaction/" + groupIdStr;
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }
  }

  @RequestMapping(method = RequestMethod.POST, value = "/invite/{personId}")
  public String invitePersonToGroup(
    @PathVariable("id") String groupIdStr,
    @PathVariable("personId") String personIdStr
  ) {
    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    Long groupId, personId;

    try {
      personId = Long.parseLong(personIdStr);
      groupId = Long.parseLong(groupIdStr);
      joinRequestService.inviteToGroup(
        authenticatedPerson.getId(),
        personId,
        groupId
      );
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    return "redirect:/profile/" + personId;
  }
}
