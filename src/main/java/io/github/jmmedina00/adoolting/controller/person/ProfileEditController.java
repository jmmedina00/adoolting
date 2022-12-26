package io.github.jmmedina00.adoolting.controller.person;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.PersonInfo;
import io.github.jmmedina00.adoolting.dto.interaction.ProfilePictureFile;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    model.addAttribute("pfp", new ProfilePictureFile());
    if (!model.containsAttribute("info")) {
      model.addAttribute(
        "info",
        personService.getPersonInfo(AuthenticatedPerson.getPersonId())
      );
    }

    return "person/profile/edit";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String updatePersonInfo(
    @ModelAttribute("info") @Valid PersonInfo info
  ) {
    personService.updatePerson(AuthenticatedPerson.getPersonId(), info);
    return "redirect:/profile";
  }
}
