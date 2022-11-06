package io.github.jmmedina00.adoolting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/test")
public class TestController {

  @RequestMapping(method = RequestMethod.GET)
  public String test(
    @RequestParam(name = "name", defaultValue = "world") String name,
    Model model
  ) {
    model.addAttribute("name", name);
    return "testing";
  }
}
