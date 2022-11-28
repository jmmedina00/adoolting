package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.NewEvent;
import io.github.jmmedina00.adoolting.dto.NewGroup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/group")
public class GroupController {

  @RequestMapping(method = RequestMethod.GET)
  public String getNewGroupForm(
    @RequestParam(required = false, name = "event") String eventStr,
    Model model
  ) {
    model.addAttribute(
      "newGroup",
      eventStr == null ? new NewGroup() : new NewEvent()
    );
    return "new-group";
  }
}
