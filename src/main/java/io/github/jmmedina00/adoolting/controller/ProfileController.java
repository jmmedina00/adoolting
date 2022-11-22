package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.entity.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/profile")
public class ProfileController {
  @Autowired
  private PersonService personService;

  @RequestMapping(method = RequestMethod.GET)
  public String redirectToAuthenticatedPersonProfile() {
    Authentication authentication = SecurityContextHolder
      .getContext()
      .getAuthentication();

    if (authentication instanceof AnonymousAuthenticationToken) {
      return "redirect:/";
    }

    Long personId =
      ((PersonDetails) authentication.getPrincipal()).getPerson().getId();

    return "redirect:/profile/" + personId;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{personId}")
  public String getPersonProfile(@PathVariable Long personId, Model model) {
    Person person = personService.getPerson(personId);

    if (person == null) {
      return "redirect:/home?notfound";
    }

    PersonDetails details = new PersonDetails(person);
    if (!details.isEnabled()) {
      return "redirect:/home?notfound";
    }

    model.addAttribute("person", person);
    return "profile";
  }
}
