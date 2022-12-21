package io.github.jmmedina00.adoolting.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Profile("backoffice")
@RequestMapping("/")
public class BackofficeController {

  @RequestMapping(method = RequestMethod.GET)
  public String getBackoffice() {
    return "backoffice";
  }
}
