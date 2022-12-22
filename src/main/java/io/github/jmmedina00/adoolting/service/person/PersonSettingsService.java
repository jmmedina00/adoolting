package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.dto.person.SettingsForm;
import io.github.jmmedina00.adoolting.entity.enums.NotificationSetting;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PersonSettings;
import io.github.jmmedina00.adoolting.repository.person.PersonSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonSettingsService {
  @Autowired
  private PersonSettingsRepository settingsRepository;

  public static final int ENTER_PROFILE = 1;
  public static final int COMMENT_ON_INTERACTION = 2;
  public static final int INVITE_TO_GROUP = 3;
  public static final int AUTO_ACCEPT_INVITE = 4;
  public static final int EMAIL_CONFIRMABLE = 5;

  public static final int NOTIFY_COMMENT = 20;
  public static final int NOTIFY_POST_FROM_OTHER = 21;
  public static final int NOTIFY_PAGE_INTERACTION = 22;

  public PersonSettings createSettingsForPerson(Person person) {
    PersonSettings settings = new PersonSettings();
    settings.setPerson(person);
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

  public NotificationSetting getNotificationSetting(Long personId, int code) {
    PersonSettings settings = settingsRepository
      .findByPersonId(personId)
      .orElse(null);
    if (settings == null) return NotificationSetting.NONE;
    NotificationSetting setting;

    switch (code) {
      case NOTIFY_COMMENT:
        setting = settings.getNotifyComments();
        break;
      case NOTIFY_POST_FROM_OTHER:
        setting = settings.getNotifyPostsFromOthers();
        break;
      case NOTIFY_PAGE_INTERACTION:
        setting = settings.getNotifyActivityFromPages();
        break;
      default:
        setting = NotificationSetting.NONE;
    }

    return setting;
  }

  public boolean isAllowedByPerson(Long personId, int desiredAction) {
    PersonSettings settings = settingsRepository
      .findByPersonId(personId)
      .orElse(null);
    if (settings == null) return false;
    boolean permission;

    switch (desiredAction) {
      case ENTER_PROFILE:
        permission = settings.isAllowStrangersIntoProfile();
        break;
      case COMMENT_ON_INTERACTION:
        permission = settings.isAllowPostsAndCommentsFromStrangers();
        break;
      case INVITE_TO_GROUP:
        permission = settings.isAllowInvitesFromStrangers();
        break;
      case AUTO_ACCEPT_INVITE:
        permission = settings.isAcceptInvitesAutomatically();
        break;
      case EMAIL_CONFIRMABLE:
        permission = settings.isEmailOnConfirmables();
        break;
      default:
        permission = false;
    }

    return permission;
  }
}
