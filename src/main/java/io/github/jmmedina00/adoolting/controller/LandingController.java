package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.User;
import io.github.jmmedina00.adoolting.exception.EmailIsUsedException;
import io.github.jmmedina00.adoolting.exception.InvalidDTOException;
import io.github.jmmedina00.adoolting.exception.TokenExpiredException;
import io.github.jmmedina00.adoolting.service.ConfirmationService;
import io.github.jmmedina00.adoolting.service.PersonService;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
  public String confirmToken(@PathVariable("token") String token) {
    String query = "?success";

    try {
      confirmationService.confirmToken(token);
    } catch (TokenExpiredException e) {
      query = "?expired";
    }

    return "redirect:/" + query;
  }

  @RequestMapping(method = RequestMethod.POST, value = "/register")
  public String registerNewPerson(
    @ModelAttribute("user") @Valid User user,
    BindingResult result,
    RedirectAttributes attributes
  ) {
    try {
      return tryToRegisterPerson(user, result, attributes);
    } catch (InvalidDTOException e) {
      return failToRegisterPerson(user, result, attributes, e);
    }
  }

  private String failToRegisterPerson(
    User user,
    BindingResult result,
    RedirectAttributes attributes,
    InvalidDTOException e
  ) {
    // Preserve errors and originally input values when redirecting
    attributes.addFlashAttribute(
      "org.springframework.validation.BindingResult.user",
      result
    );
    attributes.addFlashAttribute("user", user);

    if (e instanceof EmailIsUsedException) {
      result.rejectValue("email", "error.email.used");
    }

    return "redirect:";
  }

  private String tryToRegisterPerson(
    User user,
    BindingResult result,
    RedirectAttributes attributes
  )
    throws InvalidDTOException {
    if (!result.hasErrors()) {
      personService.createPersonFromUser(user);
      return "valid";
    }

    for (ObjectError err : result.getGlobalErrors()) {
      switch (Optional.of(err.getCode()).orElse("")) {
        case "EmailMatches":
          result.rejectValue("confirmEmail", "error.email.confirm");
          break;
        case "PasswordMatches":
          result.rejectValue("confirmPassword", "error.password.confirm");
          break;
      }
    }
    throw new InvalidDTOException();
  }
}
