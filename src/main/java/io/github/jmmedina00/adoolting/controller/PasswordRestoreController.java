package io.github.jmmedina00.adoolting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/restore-password")
public class PasswordRestoreController {

  @RequestMapping(method = RequestMethod.GET)
  public String getSendLinkForm() {
    return "forgot-password";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String sendRestoreLink() {
    return "email-sent";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{token}")
  public String getRestoreForm(@PathVariable("token") String token) {
    return "restore-password";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{token}")
  public String restorePassword(@PathVariable("token") String token) {
    return "redirect:/";
  }
}
