package io.github.jmmedina00.adoolting.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class MainController {

  private boolean isAuthenticated() {
    Authentication auth = SecurityContextHolder
      .getContext()
      .getAuthentication();
    if (
      auth == null ||
      AnonymousAuthenticationToken.class.isAssignableFrom(auth.getClass())
    ) {
      return false;
    }

    return auth.isAuthenticated();
  }

  @RequestMapping(method = RequestMethod.GET)
  public String hello() {
    if (isAuthenticated()) {
      return "redirect:test";
    }

    return "hello";
  }
}
