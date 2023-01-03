package io.github.jmmedina00.adoolting.service.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.dto.person.SettingsForm;
import io.github.jmmedina00.adoolting.entity.enums.NotificationSetting;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PersonSettings;
import io.github.jmmedina00.adoolting.repository.person.PersonSettingsRepository;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PersonSettingsServiceTest {
  @MockBean
  private PersonSettingsRepository settingsRepository;

  @Autowired
  private PersonSettingsService settingsService;

  @Captor
  private ArgumentCaptor<PersonSettings> settingsCaptor;

  @Test
  public void createSettingsForPersonCreatesNewInstanceWithAllDefaults() {
    Mockito
      .when(settingsRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    Person person = new Person();
    PersonSettings settings = settingsService.createSettingsForPerson(person);
    PersonSettings expected = new PersonSettings();

    assertEquals(person, settings.getPerson());
    assertEquals(
      expected.isAllowStrangersIntoProfile(),
      settings.isAllowStrangersIntoProfile()
    );
    assertEquals(
      expected.isAllowPostsAndCommentsFromStrangers(),
      settings.isAllowPostsAndCommentsFromStrangers()
    );
    assertEquals(
      expected.isAllowInvitesFromStrangers(),
      settings.isAllowInvitesFromStrangers()
    );
    assertEquals(
      expected.isAcceptInvitesAutomatically(),
      settings.isAcceptInvitesAutomatically()
    );
    assertEquals(
      expected.isEmailOnConfirmables(),
      settings.isEmailOnConfirmables()
    );
    assertEquals(
      expected.getNotifyIncomingEvents(),
      settings.getNotifyIncomingEvents()
    );
    assertEquals(expected.getNotifyComments(), settings.getNotifyComments());
    assertEquals(
      expected.getNotifyActivityFromPages(),
      settings.getNotifyActivityFromPages()
    );
    assertEquals(
      expected.getNotifyPeoplesBirthdays(),
      settings.getNotifyPeoplesBirthdays()
    );
  }

  @Test
  public void getSettingsFormForPersonGetsExistingSettingsForPerson() {
    PersonSettings settings = new PersonSettings();
    settings.setNotifyComments(NotificationSetting.EMAIL);
    settings.setNotifyActivityFromPages(NotificationSetting.NONE);
    settings.setAllowInvitesFromStrangers(true);
    settings.setEmailOnConfirmables(false);

    Mockito
      .when(settingsRepository.findByPersonId(4L))
      .thenReturn(Optional.of(settings));

    Person person = new Person();
    person.setId(4L);

    SettingsForm form = settingsService.getSettingsFormForPerson(person);
    assertEquals(
      settings.isAllowStrangersIntoProfile(),
      form.getAllowStrangersIntoProfile()
    );
    assertEquals(
      settings.isAllowPostsAndCommentsFromStrangers(),
      form.getAllowPostsAndCommentsFromStrangers()
    );
    assertEquals(
      settings.isAllowInvitesFromStrangers(),
      form.getAllowInvitesFromStrangers()
    );
    assertEquals(
      settings.isAcceptInvitesAutomatically(),
      form.getAcceptInvitesAutomatically()
    );
    assertEquals(
      settings.isEmailOnConfirmables(),
      form.getEmailOnConfirmables()
    );
    assertEquals(
      settings.getNotifyIncomingEvents(),
      form.getNotifyIncomingEvents()
    );
    assertEquals(settings.getNotifyComments(), form.getNotifyComments());
    assertEquals(
      settings.getNotifyActivityFromPages(),
      form.getNotifyActivityFromPages()
    );
    assertEquals(
      settings.getNotifyPeoplesBirthdays(),
      form.getNotifyPeoplesBirthdays()
    );

    verify(settingsRepository, never()).save(any());
  }

  @Test
  public void getSettingsFormForPersonCreatesSettingsForPersonIfNeeded() {
    Person person = new Person();
    person.setId(4L);
    Mockito
      .when(settingsRepository.findByPersonId(4L))
      .thenReturn(Optional.empty());
    Mockito
      .when(settingsRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    settingsService.getSettingsFormForPerson(person);
    verify(settingsRepository, times(1)).save(settingsCaptor.capture());

    PersonSettings settings = settingsCaptor.getValue();
    assertEquals(person, settings.getPerson());
  }

  @Test
  public void setSettingsForPersonUpdatesSettingsWithDetailsFromForm() {
    SettingsForm form = new SettingsForm();
    form.setNotifyComments(NotificationSetting.EMAIL);
    form.setNotifyActivityFromPages(NotificationSetting.NONE);
    form.setAllowInvitesFromStrangers(true);
    form.setEmailOnConfirmables(false);

    PersonSettings settings = new PersonSettings();
    Mockito
      .when(settingsRepository.findByPersonId(4L))
      .thenReturn(Optional.of(settings));
    Mockito
      .when(settingsRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    PersonSettings returned = settingsService.setSettingsForPerson(4L, form);
    assertEquals(
      form.getAllowStrangersIntoProfile(),
      returned.isAllowStrangersIntoProfile()
    );
    assertEquals(
      form.getAllowPostsAndCommentsFromStrangers(),
      returned.isAllowPostsAndCommentsFromStrangers()
    );
    assertEquals(
      form.getAllowInvitesFromStrangers(),
      returned.isAllowInvitesFromStrangers()
    );
    assertEquals(
      form.getAcceptInvitesAutomatically(),
      returned.isAcceptInvitesAutomatically()
    );
    assertEquals(
      form.getEmailOnConfirmables(),
      returned.isEmailOnConfirmables()
    );
    assertEquals(
      form.getNotifyIncomingEvents(),
      returned.getNotifyIncomingEvents()
    );
    assertEquals(form.getNotifyComments(), returned.getNotifyComments());
    assertEquals(
      form.getNotifyActivityFromPages(),
      returned.getNotifyActivityFromPages()
    );
    assertEquals(
      form.getNotifyPeoplesBirthdays(),
      returned.getNotifyPeoplesBirthdays()
    );

    assertEquals(settings, returned);
  }

  private static Stream<Arguments> notificationSettings() {
    return Stream.of(
      Arguments.of(
        PersonSettingsService.NOTIFY_COMMENT,
        NotificationSetting.NONE
      ),
      Arguments.of(
        PersonSettingsService.NOTIFY_PAGE_INTERACTION,
        NotificationSetting.IN_APP
      ),
      Arguments.of(
        PersonSettingsService.NOTIFY_POST_FROM_OTHER,
        NotificationSetting.EMAIL
      )
    );
  }

  @ParameterizedTest
  @MethodSource("notificationSettings")
  public void getNotificationSettingReturnsCorrectFieldAccordingToCode(
    int code,
    NotificationSetting expected
  ) {
    PersonSettings settings = new PersonSettings();
    settings.setNotifyComments(NotificationSetting.NONE);
    settings.setNotifyActivityFromPages(NotificationSetting.IN_APP);
    settings.setNotifyPostsFromOthers(NotificationSetting.EMAIL);

    Mockito
      .when(settingsRepository.findByPersonId(4L))
      .thenReturn(Optional.of(settings));

    NotificationSetting actual = settingsService.getNotificationSetting(
      4L,
      code
    );
    assertEquals(expected, actual);
  }

  @Test
  public void getNotificationSettingReturnsNoneByDefaultWhenGivenABogusCode() {
    PersonSettings settings = new PersonSettings();
    settings.setNotifyComments(NotificationSetting.EMAIL);
    settings.setNotifyActivityFromPages(NotificationSetting.IN_APP);
    settings.setNotifyPostsFromOthers(NotificationSetting.EMAIL);

    Mockito
      .when(settingsRepository.findByPersonId(4L))
      .thenReturn(Optional.of(settings));

    NotificationSetting actual = settingsService.getNotificationSetting(
      4L,
      999
    );
    assertEquals(NotificationSetting.NONE, actual);
  }

  @Test
  public void getNotificationSettingReturnsNoneWhenSettingsAreNotFound() {
    Mockito
      .when(settingsRepository.findByPersonId(4L))
      .thenReturn(Optional.empty());

    NotificationSetting actual = settingsService.getNotificationSetting(
      4L,
      PersonSettingsService.NOTIFY_COMMENT
    );
    assertEquals(NotificationSetting.NONE, actual);
  }

  private static Stream<Arguments> allowanceSettings() {
    return Stream.of(
      Arguments.of(PersonSettingsService.ENTER_PROFILE, true),
      Arguments.of(PersonSettingsService.COMMENT_ON_INTERACTION, false),
      Arguments.of(PersonSettingsService.INVITE_TO_GROUP, false),
      Arguments.of(PersonSettingsService.AUTO_ACCEPT_INVITE, true),
      Arguments.of(PersonSettingsService.EMAIL_CONFIRMABLE, false)
    );
  }

  @ParameterizedTest
  @MethodSource("allowanceSettings")
  public void isAllowedByPersonReturnsCorrectFieldDependingOnDesiredAction(
    int desiredAction,
    boolean expected
  ) {
    PersonSettings settings = new PersonSettings();
    settings.setAllowStrangersIntoProfile(true);
    settings.setAllowPostsAndCommentsFromStrangers(false);
    settings.setAllowInvitesFromStrangers(false);
    settings.setAcceptInvitesAutomatically(true);
    settings.setEmailOnConfirmables(false);

    Mockito
      .when(settingsRepository.findByPersonId(4L))
      .thenReturn(Optional.of(settings));

    boolean actual = settingsService.isAllowedByPerson(4L, desiredAction);
    assertEquals(expected, actual);
  }

  @Test
  public void isAllowedByPersonReturnsFalseWhenGivenABogusCode() {
    PersonSettings settings = new PersonSettings();
    settings.setAllowStrangersIntoProfile(true);
    settings.setAllowPostsAndCommentsFromStrangers(true);
    settings.setAllowInvitesFromStrangers(true);
    settings.setAcceptInvitesAutomatically(true);
    settings.setEmailOnConfirmables(true);

    Mockito
      .when(settingsRepository.findByPersonId(4L))
      .thenReturn(Optional.of(settings));
    assertFalse(settingsService.isAllowedByPerson(4L, 999));
  }

  @Test
  public void isAllowedByPersonReturnsFalseWhenSettingsAreNotFound() {
    Mockito
      .when(settingsRepository.findByPersonId(4L))
      .thenReturn(Optional.empty());
    assertFalse(
      settingsService.isAllowedByPerson(4L, PersonSettingsService.ENTER_PROFILE)
    );
  }
}
