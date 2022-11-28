package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.NewEvent;
import io.github.jmmedina00.adoolting.dto.NewGroup;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/group")
public class GroupController {

  @RequestMapping(method = RequestMethod.GET)
  public String getNewGroupForm(
    @RequestParam(required = false, name = "event") String eventStr,
    Model model
  ) {
    if (!model.containsAttribute("newGroup")) {
      model.addAttribute(
        "newGroup",
        eventStr == null ? new NewGroup() : new NewEvent()
      );
    }

    return "new-group";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String createNewGroup(
    @ModelAttribute("newGroup") @Valid NewGroup newGroup,
    BindingResult result,
    RedirectAttributes attributes
  ) {
    if (result.hasErrors()) {
      attributes.addFlashAttribute(
        "org.springframework.validation.BindingResult.newGroup",
        result
      );
      attributes.addFlashAttribute("newGroup", newGroup);
      return "redirect:/group";
    }

    return "redirect:/group?success";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/event")
  public String createNewEvent(
    @ModelAttribute("newGroup") @Valid NewEvent newEvent,
    BindingResult result,
    RedirectAttributes attributes
  ) {
    System.out.println(newEvent.getDate());
    System.out.println(newEvent.getTime());

    if (result.hasErrors()) {
      attributes.addFlashAttribute(
        "org.springframework.validation.BindingResult.newGroup",
        result
      );
      attributes.addFlashAttribute("newGroup", newEvent);
      return "redirect:/group?event";
    }

    return "redirect:/group?success=1&event=1";
  }
}
