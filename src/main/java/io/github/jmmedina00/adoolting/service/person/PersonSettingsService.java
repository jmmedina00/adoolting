package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.dto.person.SettingsForm;
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

  public SettingsForm getSettingsFormForPerson(Person person) {
    PersonSettings settings = settingsRepository
      .findByPersonId(person.getId())
      .orElseGet(() -> createSettingsForPerson(person)); // Run creation ONLY when needed
    SettingsForm form = new SettingsForm();

    form.setAllowStrangersIntoProfile(settings.isAllowStrangersIntoProfile());
    form.setAllowPostsAndCommentsFromStrangers(
      settings.isAllowPostsAndCommentsFromStrangers()
    );
    form.setAllowInvitesFromStrangers(settings.isAllowInvitesFromStrangers());
    form.setAcceptInvitesAutomatically(settings.isAcceptInvitesAutomatically());
    form.setEmailOnConfirmables(settings.isEmailOnConfirmables());
    form.setNotifyIncomingEvents(settings.getNotifyIncomingEvents());
    form.setNotifyComments(settings.getNotifyComments());
    form.setNotifyPostsFromOthers(settings.getNotifyPostsFromOthers());
    form.setNotifyActivityFromPages(settings.getNotifyActivityFromPages());
    form.setNotifyPeoplesBirthdays(settings.getNotifyPeoplesBirthdays());

    return form;
  }

  public PersonSettings setSettingsForPerson(Long personId, SettingsForm form) {
    PersonSettings settings = settingsRepository.findByPersonId(personId).get();

    settings.setAllowStrangersIntoProfile(form.getAllowStrangersIntoProfile());
    settings.setAllowPostsAndCommentsFromStrangers(
      form.getAllowPostsAndCommentsFromStrangers()
    );
    settings.setAllowInvitesFromStrangers(form.getAllowInvitesFromStrangers());
    settings.setAcceptInvitesAutomatically(
      form.getAcceptInvitesAutomatically()
    );
    settings.setEmailOnConfirmables(form.getEmailOnConfirmables());
    settings.setNotifyIncomingEvents(form.getNotifyIncomingEvents());
    settings.setNotifyComments(form.getNotifyComments());
    settings.setNotifyPostsFromOthers(form.getNotifyPostsFromOthers());
    settings.setNotifyActivityFromPages(form.getNotifyActivityFromPages());
    settings.setNotifyPeoplesBirthdays(form.getNotifyPeoplesBirthdays());

    return settingsRepository.save(settings);
  }
}
