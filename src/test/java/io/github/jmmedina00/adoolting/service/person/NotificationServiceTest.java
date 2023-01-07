package io.github.jmmedina00.adoolting.service.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.enums.NotificationSetting;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.person.Notification;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.person.NotificationRepository;
import io.github.jmmedina00.adoolting.service.util.EmailService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
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
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class NotificationServiceTest {
  @MockBean
  private NotificationRepository notificationRepository;

  @MockBean
  private PersonSettingsService settingsService;

  @MockBean
  private EmailService emailService;

  @Autowired
  private NotificationService notificationService;

  @Captor
  private ArgumentCaptor<Notification> notificationCaptor;

  @Test
  public void markNotificationAsReadAddsADateToReadAtInNotificationIfNonePresent() {
    Notification notification = new Notification();
    notification.setReadAt(null);

    Mockito
      .when(notificationRepository.findBelongingNotification(125L, 4L))
      .thenReturn(Optional.of(notification));
    Mockito
      .when(notificationRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Notification returned = notificationService.markNotificationAsRead(
      125L,
      4L
    );
    assertNotNull(returned.getReadAt());
    assertEquals(notification, returned);
  }

  @Test
  public void markNotificationAsReadDoesNotChangeReadAtDateIfNotNull() {
    Notification notification = new Notification();
    notification.setReadAt(new Date(1000L));

    Mockito
      .when(notificationRepository.findBelongingNotification(125L, 4L))
      .thenReturn(Optional.of(notification));
    Mockito
      .when(notificationRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Notification returned = notificationService.markNotificationAsRead(
      125L,
      4L
    );
    assertEquals(new Date(1000L), returned.getReadAt());
  }

  @Test
  public void markNotificationAsReadThrowsIfNotificationIsNotFound() {
    Mockito
      .when(notificationRepository.findBelongingNotification(125L, 4L))
      .thenReturn(Optional.empty());

    assertThrows(
      NoSuchElementException.class,
      () -> {
        notificationService.markNotificationAsRead(125L, 4L);
      }
    );
  }

  @Test
  public void deleteNotificationSetsNotificationDeletedAt() {
    Notification notification = new Notification();
    Mockito
      .when(notificationRepository.findBelongingNotification(125L, 4L))
      .thenReturn(Optional.of(notification));
    Mockito
      .when(notificationRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Notification deleted = notificationService.deleteNotification(125L, 4L);
    assertNotNull(deleted.getDeletedAt());
    assertEquals(notification, deleted);
  }

  @Test
  public void createNotificationsOnlyCreatesNewRecordWhenNotificationSettingSetToInApp() {
    int code = PersonSettingsService.NOTIFY_POST_FROM_OTHER;

    Post post = new Post();
    Person creator = new Person();
    creator.setId(13L);
    Person notified = new Person();
    notified.setId(14L);

    post.setInteractor(creator);

    Mockito
      .when(settingsService.getNotificationSetting(14L, code))
      .thenReturn(NotificationSetting.IN_APP);
    Mockito
      .when(notificationRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    notificationService.createNotifications(post, notified, code);

    verify(notificationRepository, times(1)).save(notificationCaptor.capture());

    Notification notification = notificationCaptor.getValue();
    assertEquals(notified, notification.getForPerson());
    assertEquals(post, notification.getInteraction());

    verify(emailService, never()).setUpEmailJob(eq(notification), anyString());
    verify(settingsService, never())
      .isAllowedByPerson(14L, PersonSettingsService.EMAIL_CONFIRMABLE);
  }

  private static Stream<Arguments> notifCodeAndTemplateCombos() {
    return Stream.of(
      Arguments.of(PersonSettingsService.NOTIFY_COMMENT, "comment"),
      Arguments.of(
        PersonSettingsService.NOTIFY_PAGE_INTERACTION,
        "page-activity"
      ),
      Arguments.of(PersonSettingsService.NOTIFY_POST_FROM_OTHER, "new-post")
    );
  }

  @ParameterizedTest
  @MethodSource("notifCodeAndTemplateCombos")
  public void createNotificationsCreatesNotificationAndCorrectEmailWhenSettingSetToEmail(
    int code,
    String expectedTemplate
  ) {
    Post post = new Post();
    Person creator = new Person();
    creator.setId(13L);
    Person notified = new Person();
    notified.setId(14L);

    post.setInteractor(creator);

    Mockito
      .when(settingsService.getNotificationSetting(14L, code))
      .thenReturn(NotificationSetting.EMAIL);
    Mockito
      .when(notificationRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    notificationService.createNotifications(post, notified, code);

    verify(notificationRepository, times(1)).save(notificationCaptor.capture());

    Notification notification = notificationCaptor.getValue();
    assertEquals(notified, notification.getForPerson());
    assertEquals(post, notification.getInteraction());

    verify(emailService, times(1))
      .setUpEmailJob(notification, expectedTemplate);
    verify(settingsService, never())
      .isAllowedByPerson(14L, PersonSettingsService.EMAIL_CONFIRMABLE);
  }

  @Test
  public void createNotificationsDoesNothingIfSettingIsSetToNone() {
    int code = PersonSettingsService.NOTIFY_POST_FROM_OTHER;

    Post post = new Post();
    Person creator = new Person();
    creator.setId(13L);
    Person notified = new Person();
    notified.setId(14L);

    post.setInteractor(creator);

    Mockito
      .when(settingsService.getNotificationSetting(14L, code))
      .thenReturn(NotificationSetting.NONE);
    Mockito
      .when(notificationRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    notificationService.createNotifications(post, notified, code);

    verify(notificationRepository, never()).save(any());
    verify(emailService, never()).setUpEmailJob(any(Notification.class), any());
    verify(settingsService, never())
      .isAllowedByPerson(14L, PersonSettingsService.EMAIL_CONFIRMABLE);
  }

  @Test
  public void createNotificationsAlwaysCreatesNotificationForConfirmableButSendsNoEmailIfNotDesiredByPerson() {
    Person sender = new Person();
    sender.setId(14L);
    Person receiever = new Person();
    receiever.setId(15L);
    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setInteractor(sender);
    interaction.setReceiverInteractor(receiever);

    Mockito
      .when(
        settingsService.isAllowedByPerson(
          15L,
          PersonSettingsService.EMAIL_CONFIRMABLE
        )
      )
      .thenReturn(false);
    Mockito
      .when(notificationRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    notificationService.createNotifications(interaction, receiever, 0);

    verify(notificationRepository, times(1)).save(notificationCaptor.capture());
    Notification notification = notificationCaptor.getValue();
    assertEquals(receiever, notification.getForPerson());
    assertEquals(interaction, notification.getInteraction());

    verify(emailService, never()).setUpEmailJob(eq(notification), anyString());
    verify(settingsService, never()).getNotificationSetting(14L, 0);
  }

  @Test
  public void createNotificationsCreatesNotificationAndSendsEmailForReceiver() {
    Person sender = new Person();
    sender.setId(14L);
    Person receiever = new Person();
    receiever.setId(15L);
    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setInteractor(sender);
    interaction.setReceiverInteractor(receiever);

    Mockito
      .when(
        settingsService.isAllowedByPerson(
          15L,
          PersonSettingsService.EMAIL_CONFIRMABLE
        )
      )
      .thenReturn(true);
    Mockito
      .when(notificationRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    notificationService.createNotifications(interaction, receiever, 0);

    verify(notificationRepository, times(1)).save(notificationCaptor.capture());
    Notification notification = notificationCaptor.getValue();
    assertEquals(receiever, notification.getForPerson());
    assertEquals(interaction, notification.getInteraction());

    verify(emailService, times(1)).setUpEmailJob(notification, "pending");
    verify(settingsService, never()).getNotificationSetting(14L, 0);
  }

  @Test
  public void createNotificationsCreatesNotificationAndSendsEmailForSender() {
    Person sender = new Person();
    sender.setId(14L);
    Person receiever = new Person();
    receiever.setId(15L);
    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setInteractor(sender);
    interaction.setReceiverInteractor(receiever);

    Mockito
      .when(
        settingsService.isAllowedByPerson(
          14L,
          PersonSettingsService.EMAIL_CONFIRMABLE
        )
      )
      .thenReturn(true);
    Mockito
      .when(notificationRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    notificationService.createNotifications(interaction, sender, 0);

    verify(notificationRepository, times(1)).save(notificationCaptor.capture());
    Notification notification = notificationCaptor.getValue();
    assertEquals(sender, notification.getForPerson());
    assertEquals(interaction, notification.getInteraction());

    verify(emailService, times(1)).setUpEmailJob(notification, "accepted");
    verify(settingsService, never()).getNotificationSetting(14L, 0);
  }
}
