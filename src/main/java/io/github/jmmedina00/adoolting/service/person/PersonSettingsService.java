package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PersonSettings;
import io.github.jmmedina00.adoolting.repository.person.PersonSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PersonSettingsService {
  @Autowired
  private PersonSettingsRepository settingsRepository;

  public PersonSettings createSettingsForPerson(Person person) {
    PersonSettings settings = new PersonSettings();
    settings.setPerson(person);
    settings.setLocale(LocaleContextHolder.getLocale().toString());
    return settingsRepository.save(settings);
  }
}
