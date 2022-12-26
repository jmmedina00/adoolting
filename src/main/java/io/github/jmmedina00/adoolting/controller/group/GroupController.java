package io.github.jmmedina00.adoolting.controller.group;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.group.NewEvent;
import io.github.jmmedina00.adoolting.dto.group.NewGroup;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.service.group.PeopleGroupService;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import java.util.ArrayList;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/group")
public class GroupController {
  @Autowired
  private PeopleGroupService groupService;

  @Autowired
  private PageService pageService;

  @Autowired
  private PersonService personService;

  @RequestMapping(method = RequestMethod.GET)
  public String getNewGroupForm(
    @RequestParam(required = false, name = "event") String eventStr,
    Model model
  ) {
    Long personId = AuthenticatedPerson.getPersonId();
    ArrayList<Interactor> controlledInteractors = new ArrayList<>(
      pageService.getAllPersonPages(personId)
    );
    controlledInteractors.add(0, personService.getPerson(personId));

    if (!model.containsAttribute("newGroup")) {
      model.addAttribute(
        "newGroup",
        eventStr == null ? new NewGroup() : new NewEvent()
      );
    }
    model.addAttribute("interactors", controlledInteractors);

    return "group/new";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String createNewGroup(
    @ModelAttribute("newGroup") @Valid NewGroup newGroup
  ) {
    PeopleGroup group = groupService.createGroup(
      newGroup,
      AuthenticatedPerson.getPersonId()
    );
    return "redirect:/interaction/" + group.getId();
  }
}
