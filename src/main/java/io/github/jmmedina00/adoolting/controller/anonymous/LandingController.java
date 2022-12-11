package io.github.jmmedina00.adoolting.controller.anonymous;

import io.github.jmmedina00.adoolting.dto.User;
import io.github.jmmedina00.adoolting.exception.TokenExpiredException;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import io.github.jmmedina00.adoolting.service.util.ConfirmationService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class LandingController {
  @Autowired
  private PersonService personService;

  @Autowired
  private ConfirmationService confirmationService;

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
  public String hello(Model model) {
    if (isAuthenticated()) {
      return "redirect:/home";
    }

    // Might have been redirected from invalid register
    if (!model.containsAttribute("user")) {
      User userDto = new User();
      model.addAttribute("user", userDto);
    }

    return "hello";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/confirm/{token}")
  public String confirmToken(@PathVariable("token") String token)
    throws TokenExpiredException {
    confirmationService.confirmToken(token);
    return "redirect:/?success";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/register")
  public String registerNewPerson(@ModelAttribute("user") @Valid User user)
    throws BindException {
    personService.createPersonFromUser(user);
    return "valid";
  }
}
