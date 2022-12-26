package io.github.jmmedina00.adoolting.controller.person;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.person.SettingsForm;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import io.github.jmmedina00.adoolting.service.person.PersonSettingsService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/settings")
public class SettingsController {
  @Autowired
  private PersonSettingsService settingsService;

  @Autowired
  private PersonService personService;

  @RequestMapping(method = RequestMethod.GET)
  public String getSettingsForm(Model model) {
    Person person = personService.getPerson(AuthenticatedPerson.getPersonId());

    if (!model.containsAttribute("settings")) {
      model.addAttribute(
        "settings",
        settingsService.getSettingsFormForPerson(person)
      );
    }
    return "person/settings";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String updateSettings(
    @ModelAttribute("settings") @Valid SettingsForm form
  ) {
    settingsService.setSettingsForPerson(
      AuthenticatedPerson.getPersonId(),
      form
    );
    return "redirect:/settings";
  }
}
