package io.github.jmmedina00.adoolting.service.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.Event;
import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.group.JoinRequestRepository;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.person.PersonSettingsService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class JoinRequestServiceTest {
  @MockBean
  private JoinRequestRepository joinRequestRepository;

  @MockBean
  private PeopleGroupService groupService;

  @MockBean
  private InteractorService interactorService;

  @MockBean
  private InteractionService interactionService;

  @MockBean
  private ConfirmableInteractionService cInteractionService;

  @MockBean
  private PersonSettingsService settingsService;

  @Autowired
  private JoinRequestService joinRequestService;

  @Test
  public void getGroupMembersReturnsInteractorInEachJoinRequestWhichIsNotGroupCreator() {
    Person creator = new Person();
    creator.setId(14L);
    PeopleGroup group = new PeopleGroup();
    group.setInteractor(creator);

    Person foo = new Person();
    foo.setId(15L);

    JoinRequest reqFoo = new JoinRequest();
    reqFoo.setInteractor(foo);
    reqFoo.setReceiverInteractor(creator);
    reqFoo.setConfirmedAt(new Date());

    Person bar = new Person();
    bar.setId(16L);

    JoinRequest reqBar = new JoinRequest();
    reqBar.setInteractor(creator);
    reqBar.setReceiverInteractor(bar);
    reqBar.setConfirmedAt(new Date());

    Person baz = new Person();
    baz.setId(17L);

    JoinRequest reqBaz = new JoinRequest();
    reqBaz.setInteractor(creator);
    reqBaz.setReceiverInteractor(baz);

    Person bat = new Person();
    bat.setId(18L);

    JoinRequest reqBat = new JoinRequest();
    reqBat.setInteractor(bat);
    reqBat.setReceiverInteractor(creator);
    reqBat.setConfirmedAt(new Date());

    Mockito.when(groupService.getGroup(22L)).thenReturn(group);
    Mockito
      .when(joinRequestRepository.findExistingForGroup(22L))
      .thenReturn(List.of(reqFoo, reqBar, reqBaz, reqBat));

    List<Interactor> result = joinRequestService.getGroupMembers(22L);
    assertEquals(List.of(foo, bar, bat), result);
  }

  @Test
  public void getGroupMembersReturnsInteractorInEachJoinRequestWhichIsNotEventCreator() {
    Page creator = new Page();
    creator.setId(14L);
    Event group = new Event();
    group.setInteractor(creator);

    Person foo = new Person();
    foo.setId(15L);

    JoinRequest reqFoo = new JoinRequest();
    reqFoo.setInteractor(foo);
    reqFoo.setReceiverInteractor(creator);
    reqFoo.setConfirmedAt(new Date());

    Person bar = new Person();
    bar.setId(16L);

    JoinRequest reqBar = new JoinRequest();
    reqBar.setInteractor(creator);
    reqBar.setReceiverInteractor(bar);
    reqBar.setConfirmedAt(new Date());

    Person baz = new Person();
    baz.setId(17L);

    JoinRequest reqBaz = new JoinRequest();
    reqBaz.setInteractor(creator);
    reqBaz.setReceiverInteractor(baz);

    Person bat = new Person();
    bat.setId(18L);

    JoinRequest reqBat = new JoinRequest();
    reqBat.setInteractor(bat);
    reqBat.setReceiverInteractor(creator);
    reqBat.setConfirmedAt(new Date());

    Mockito.when(groupService.getGroup(22L)).thenReturn(group);
    Mockito
      .when(joinRequestRepository.findExistingForGroup(22L))
      .thenReturn(List.of(reqFoo, reqBar, reqBaz, reqBat));

    List<Interactor> result = joinRequestService.getGroupMembers(22L);
    assertEquals(List.of(foo, bar, bat), result);
  }

  @Test
  public void isMemberOfGroupReturnsTrueIfPersonManagesGroupCreator() {
    Page creator = new Page();
    creator.setId(14L);
    Event event = new Event();
    event.setInteractor(creator);

    Mockito.when(groupService.getGroup(22L)).thenReturn(event);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(14L, 5L))
      .thenReturn(true);

    assertTrue(joinRequestService.isMemberOfGroup(22L, 5L));
  }

  @Test
  public void isMemberOfGroupReturnsTrueIfPersonDoesNotManageGroupCreatorButIsMemberOfGroup() {
    Person creator = new Person();
    creator.setId(14L);
    Event event = new Event();
    event.setInteractor(creator);

    Person foo = new Person();
    foo.setId(15L);

    JoinRequest reqFoo = new JoinRequest();
    reqFoo.setInteractor(foo);
    reqFoo.setReceiverInteractor(creator);
    reqFoo.setConfirmedAt(new Date());

    Person bar = new Person();
    bar.setId(16L);
    JoinRequest reqBar = new JoinRequest();
    reqBar.setInteractor(creator);
    reqBar.setReceiverInteractor(bar);

    Mockito.when(groupService.getGroup(22L)).thenReturn(event);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(14L, 15L))
      .thenReturn(false);
    Mockito
      .when(joinRequestRepository.findExistingForGroup(22L))
      .thenReturn(List.of(reqFoo, reqBar));

    assertTrue(joinRequestService.isMemberOfGroup(22L, 15L));
  }

  @Test
  public void isMemberOfGroupReturnsFalseIfPersonDoesNotManageGroupCreatorAndIsNotMemberOfGroup() {
    Person creator = new Person();
    creator.setId(14L);
    Event event = new Event();
    event.setInteractor(creator);

    Person foo = new Person();
    foo.setId(15L);

    JoinRequest reqFoo = new JoinRequest();
    reqFoo.setInteractor(foo);
    reqFoo.setReceiverInteractor(creator);
    reqFoo.setConfirmedAt(new Date());

    Person bar = new Person();
    bar.setId(16L);
    JoinRequest reqBar = new JoinRequest();
    reqBar.setInteractor(creator);
    reqBar.setReceiverInteractor(bar);

    Mockito.when(groupService.getGroup(22L)).thenReturn(event);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(14L, 16L))
      .thenReturn(false);
    Mockito
      .when(joinRequestRepository.findExistingForGroup(22L))
      .thenReturn(List.of(reqFoo, reqBar));

    assertFalse(joinRequestService.isMemberOfGroup(22L, 16L));
  }

  @Test
  public void isMemberOfGroupReturnsFalseIfPersonDoesNotManageGroupCreatorAndHasNoJoinRequest() {
    Person creator = new Person();
    creator.setId(14L);
    Event event = new Event();
    event.setInteractor(creator);

    Person foo = new Person();
    foo.setId(15L);

    JoinRequest reqFoo = new JoinRequest();
    reqFoo.setInteractor(foo);
    reqFoo.setReceiverInteractor(creator);
    reqFoo.setConfirmedAt(new Date());

    Person bar = new Person();
    bar.setId(16L);
    JoinRequest reqBar = new JoinRequest();
    reqBar.setInteractor(creator);
    reqBar.setReceiverInteractor(bar);

    Mockito.when(groupService.getGroup(22L)).thenReturn(event);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(14L, 17L))
      .thenReturn(false);
    Mockito
      .when(joinRequestRepository.findExistingForGroup(22L))
      .thenReturn(List.of(reqFoo, reqBar));

    assertFalse(joinRequestService.isMemberOfGroup(22L, 17L));
  }

  @Test
  public void joinGroupCreatesRequestWherePersonIsCreatorAndGroupCreatorIsReceiver()
    throws NotAuthorizedException {
    PeopleGroup group = new PeopleGroup();
    Person creator = new Person();
    creator.setId(20L);

    group.setInteractor(creator);
    Person requester = new Person();
    requester.setId(24L);

    Mockito.when(groupService.getGroup(12L)).thenReturn(group);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(requester);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(20L, 24L))
      .thenReturn(false);
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    JoinRequest joinRequest = joinRequestService.joinGroup(24L, 12L);

    assertEquals(requester, joinRequest.getInteractor());
    assertEquals(group, joinRequest.getGroup());
    assertEquals(creator, joinRequest.getReceiverInteractor());

    verify(interactionService, times(1)).saveInteraction(joinRequest);
  }

  @Test
  public void joinGroupReturnsExistingJoinRequestIfItExists()
    throws NotAuthorizedException {
    PeopleGroup group = new PeopleGroup();
    Person creator = new Person();
    creator.setId(20L);

    group.setInteractor(creator);
    Person requester = new Person();
    requester.setId(24L);
    JoinRequest existingRequest = new JoinRequest();

    Mockito.when(groupService.getGroup(12L)).thenReturn(group);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(requester);
    Mockito
      .when(joinRequestRepository.findExistingForInteractorsAndGroup(24L, 12L))
      .thenReturn(existingRequest);

    JoinRequest joinRequest = joinRequestService.joinGroup(24L, 12L);

    assertEquals(existingRequest, joinRequest);

    verify(interactionService, never()).saveInteraction(any());
  }

  @Test
  public void joinGroupThrowsIfCreatorOrManagerTriesToJoinGroup() {
    Person creator = new Person();
    creator.setId(20L);

    PeopleGroup group = new PeopleGroup();
    group.setInteractor(creator);

    Mockito.when(interactorService.getInteractor(20L)).thenReturn(creator);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(20L, 20L))
      .thenReturn(true);
    Mockito.when(groupService.getGroup(12L)).thenReturn(group);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        joinRequestService.joinGroup(20L, 12L);
      }
    );
  }

  @Test
  public void inviteToGroupCreatesRequestCreatedByGroupCreator()
    throws NotAuthorizedException {
    Person host = new Person();
    host.setId(35L);
    Person invited = new Person();
    invited.setId(24L);
    PeopleGroup group = new PeopleGroup();
    group.setInteractor(host);

    ConfirmableInteraction interaction = new ConfirmableInteraction();

    Mockito
      .when(cInteractionService.getPersonFriendship(35L, 24L))
      .thenReturn(interaction);
    Mockito.when(interactorService.getInteractor(35L)).thenReturn(host);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(invited);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(35L, 24L))
      .thenReturn(false);
    Mockito
      .when(groupService.getGroupManagedByPerson(12L, 35L))
      .thenReturn(group);
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    JoinRequest joinRequest = joinRequestService.inviteToGroup(35L, 24L, 12L);

    assertEquals(host, joinRequest.getInteractor());
    assertEquals(invited, joinRequest.getReceiverInteractor());
    assertEquals(group, joinRequest.getGroup());
    assertNull(joinRequest.getConfirmedAt());

    verify(interactionService, times(1)).saveInteraction(joinRequest);
  }

  @Test
  public void inviteToGroupCreatesRequestCreatedByEventCreator()
    throws NotAuthorizedException {
    Person host = new Person();
    host.setId(35L);
    Person invited = new Person();
    invited.setId(24L);
    PeopleGroup group = new PeopleGroup();
    Page creator = new Page();
    group.setInteractor(creator);

    ConfirmableInteraction interaction = new ConfirmableInteraction();

    Mockito
      .when(cInteractionService.getPersonFriendship(35L, 24L))
      .thenReturn(interaction);
    Mockito.when(interactorService.getInteractor(35L)).thenReturn(host);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(invited);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(35L, 24L))
      .thenReturn(false);
    Mockito
      .when(groupService.getGroupManagedByPerson(12L, 35L))
      .thenReturn(group);
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    JoinRequest joinRequest = joinRequestService.inviteToGroup(35L, 24L, 12L);

    assertEquals(creator, joinRequest.getInteractor());
    assertEquals(invited, joinRequest.getReceiverInteractor());
    assertEquals(group, joinRequest.getGroup());
    assertNull(joinRequest.getConfirmedAt());

    verify(interactionService, times(1)).saveInteraction(joinRequest);
  }

  @Test
  public void inviteToGroupReturnsExistingRequestIfAny()
    throws NotAuthorizedException {
    Person host = new Person();
    host.setId(35L);
    Person invited = new Person();
    invited.setId(24L);
    JoinRequest existing = new JoinRequest();
    PeopleGroup group = new PeopleGroup();
    group.setInteractor(host);

    ConfirmableInteraction interaction = new ConfirmableInteraction();

    Mockito
      .when(cInteractionService.getPersonFriendship(35L, 24L))
      .thenReturn(interaction);

    Mockito
      .when(groupService.getGroupManagedByPerson(12L, 35L))
      .thenReturn(group);
    Mockito
      .when(joinRequestRepository.findExistingForInteractorsAndGroup(24L, 12L))
      .thenReturn(existing);
    Mockito.when(interactorService.getInteractor(35L)).thenReturn(host);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(invited);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(35L, 24L))
      .thenReturn(false);

    JoinRequest joinRequest = joinRequestService.inviteToGroup(35L, 24L, 12L);

    assertEquals(existing, joinRequest);
    verify(interactionService, never()).saveInteraction(any());
  }

  @Test
  public void inviteToGroupCanOnlyInvitePersonToGroup()
    throws NotAuthorizedException {
    Person host = new Person();
    host.setId(35L);
    Page invited = new Page();
    invited.setId(24L);
    PeopleGroup group = new PeopleGroup();
    group.setInteractor(host);

    Mockito
      .when(groupService.getGroupManagedByPerson(12L, 35L))
      .thenReturn(group);
    Mockito.when(interactorService.getInteractor(35L)).thenReturn(host);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(invited);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(35L, 24L))
      .thenReturn(false);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        joinRequestService.inviteToGroup(35L, 24L, 12L);
      }
    );
  }

  @Test
  public void inviteToGroupMayOnlyBeUsedByGroupManager()
    throws NotAuthorizedException {
    Mockito
      .when(groupService.getGroupManagedByPerson(12L, 35L))
      .thenThrow(NotAuthorizedException.class);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        joinRequestService.inviteToGroup(35L, 24L, 12L);
      }
    );
  }

  @Test
  public void inviteToGroupCannotInviteManagersOfGroupCreator()
    throws NotAuthorizedException {
    Page host = new Page();
    host.setId(35L);
    Page invited = new Page();
    invited.setId(24L);

    Page creator = new Page();
    creator.setId(28L);
    PeopleGroup group = new PeopleGroup();
    group.setInteractor(creator);

    ConfirmableInteraction interaction = new ConfirmableInteraction();

    Mockito
      .when(cInteractionService.getPersonFriendship(35L, 24L))
      .thenReturn(interaction);
    Mockito
      .when(groupService.getGroupManagedByPerson(12L, 35L))
      .thenReturn(group);
    Mockito.when(interactorService.getInteractor(35L)).thenReturn(host);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(invited);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(28L, 24L))
      .thenReturn(true);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        joinRequestService.inviteToGroup(35L, 24L, 12L);
      }
    );
  }

  @Test
  public void inviteToGroupOnlyProceedsWhenThereIsNotFriendshipIfTheInvitedPersonExplicitlyAllowsInvitesFromStrangers()
    throws NotAuthorizedException {
    Person host = new Person();
    host.setId(35L);
    Person invited = new Person();
    invited.setId(24L);
    PeopleGroup group = new PeopleGroup();
    group.setInteractor(host);

    Mockito
      .when(cInteractionService.getPersonFriendship(35L, 24L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          24L,
          PersonSettingsService.INVITE_TO_GROUP
        )
      )
      .thenReturn(true);
    Mockito.when(interactorService.getInteractor(35L)).thenReturn(host);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(invited);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(35L, 24L))
      .thenReturn(false);
    Mockito
      .when(groupService.getGroupManagedByPerson(12L, 35L))
      .thenReturn(group);
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    JoinRequest joinRequest = joinRequestService.inviteToGroup(35L, 24L, 12L);

    assertEquals(host, joinRequest.getInteractor());
    assertEquals(invited, joinRequest.getReceiverInteractor());
    assertEquals(group, joinRequest.getGroup());
    assertNull(joinRequest.getConfirmedAt());

    verify(interactionService, times(1)).saveInteraction(joinRequest);
  }

  @Test
  public void inviteToGroupThrowsWhenThereIsNoFriendshipAndInvitedPersonDisallowsInvitesFromStrangers()
    throws NotAuthorizedException {
    Person host = new Person();
    host.setId(35L);
    Person invited = new Person();
    invited.setId(24L);
    PeopleGroup group = new PeopleGroup();
    group.setInteractor(host);

    Mockito
      .when(cInteractionService.getPersonFriendship(35L, 24L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          24L,
          PersonSettingsService.INVITE_TO_GROUP
        )
      )
      .thenReturn(false);
    Mockito.when(interactorService.getInteractor(35L)).thenReturn(host);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(invited);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(35L, 24L))
      .thenReturn(false);
    Mockito
      .when(groupService.getGroupManagedByPerson(12L, 35L))
      .thenReturn(group);
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        joinRequestService.inviteToGroup(35L, 24L, 12L);
      }
    );
  }

  @Test
  public void inviteToGroupAlsoFillsInConfirmedDateIfBothPersonsAreFriendsAndInvitedHasSettingToggled()
    throws NotAuthorizedException {
    Person host = new Person();
    host.setId(35L);
    Person invited = new Person();
    invited.setId(24L);
    PeopleGroup group = new PeopleGroup();
    group.setInteractor(host);

    ConfirmableInteraction interaction = new ConfirmableInteraction();

    Mockito
      .when(cInteractionService.getPersonFriendship(35L, 24L))
      .thenReturn(interaction);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          24L,
          PersonSettingsService.AUTO_ACCEPT_INVITE
        )
      )
      .thenReturn(true);
    Mockito.when(interactorService.getInteractor(35L)).thenReturn(host);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(invited);
    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(35L, 24L))
      .thenReturn(false);
    Mockito
      .when(groupService.getGroupManagedByPerson(12L, 35L))
      .thenReturn(group);
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    JoinRequest joinRequest = joinRequestService.inviteToGroup(35L, 24L, 12L);

    assertEquals(host, joinRequest.getInteractor());
    assertEquals(invited, joinRequest.getReceiverInteractor());
    assertEquals(group, joinRequest.getGroup());
    assertNotNull(joinRequest.getConfirmedAt());

    verify(interactionService, times(2)).saveInteraction(joinRequest);
  }
}
