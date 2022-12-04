package io.github.jmmedina00.adoolting.controller.group;

import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.service.group.PeopleGroupService;
import org.springframework.beans.factory.annotation.Autowired;
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
    return "group-manage";
  }
}
