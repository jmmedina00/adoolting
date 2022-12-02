package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.PersonInfo;
import io.github.jmmedina00.adoolting.dto.interaction.ProfilePictureFile;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/profile/edit")
public class ProfileEditController {
  @Autowired
  private PersonService personService;

  @RequestMapping(method = RequestMethod.GET)
  public String getEditInfoForm(Model model) {
    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    model.addAttribute("pfp", new ProfilePictureFile());
    model.addAttribute(
      "info",
      personService.getPersonInfo(authenticatedPerson.getId())
    );
    return "profile-edit";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String updatePersonInfo(
    @ModelAttribute("info") @Valid PersonInfo info,
    BindingResult result
  ) {
    if (result.hasErrors()) {
      return "redirect:/profile/edit?error";
    }

    Long personId =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson()
        .getId();

    personService.updatePerson(personId, info);
    return "redirect:/profile";
  }
}
