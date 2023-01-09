package io.github.jmmedina00.adoolting.service.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.group.JoinRequestRepository;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
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

  @Autowired
  private JoinRequestService joinRequestService;

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
    JoinRequest existingRequest = new JoinRequest();

    Mockito
      .when(joinRequestRepository.findExistingForInteractorsAndGroup(24L, 12L))
      .thenReturn(existingRequest);

    JoinRequest joinRequest = joinRequestService.joinGroup(24L, 12L);

    assertEquals(existingRequest, joinRequest);

    verify(interactorService, never()).getInteractor(any());
    verify(groupService, never()).getGroup(any());
    verify(interactionService, never()).saveInteraction(any());
  }

  @Test
  public void joinGroupThrowsIfCreatorTriesToJoinGroup() {
    Person creator = new Person();
    creator.setId(20L);

    PeopleGroup group = new PeopleGroup();
    group.setInteractor(creator);

    Mockito.when(interactorService.getInteractor(20L)).thenReturn(creator);
    Mockito.when(groupService.getGroup(12L)).thenReturn(group);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        joinRequestService.joinGroup(20L, 12L);
      }
    );
  }

  @Test
  public void inviteToGroupCreatesRequestCreatedByAttemptingPerson()
    throws NotAuthorizedException {
    Person host = new Person();
    Person invited = new Person();
    PeopleGroup group = new PeopleGroup();

    Mockito.when(interactorService.getInteractor(35L)).thenReturn(host);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(invited);
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

    verify(interactionService, times(1)).saveInteraction(joinRequest);
  }

  @Test
  public void inviteToGroupReturnsExistingRequestIfAny()
    throws NotAuthorizedException {
    Person host = new Person();
    Person invited = new Person();
    JoinRequest existing = new JoinRequest();
    PeopleGroup group = new PeopleGroup();

    Mockito
      .when(groupService.getGroupManagedByPerson(12L, 35L))
      .thenReturn(group);
    Mockito
      .when(joinRequestRepository.findExistingForInteractorsAndGroup(24L, 12L))
      .thenReturn(existing);
    Mockito.when(interactorService.getInteractor(35L)).thenReturn(host);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(invited);

    JoinRequest joinRequest = joinRequestService.inviteToGroup(35L, 24L, 12L);

    assertEquals(existing, joinRequest);
    verify(interactionService, never()).saveInteraction(any());
  }

  @Test
  public void inviteToGroupCanOnlyInvitePersonToGroup()
    throws NotAuthorizedException {
    Person host = new Person();
    Page invited = new Page();
    PeopleGroup group = new PeopleGroup();

    Mockito
      .when(groupService.getGroupManagedByPerson(12L, 35L))
      .thenReturn(group);
    Mockito.when(interactorService.getInteractor(35L)).thenReturn(host);
    Mockito.when(interactorService.getInteractor(24L)).thenReturn(invited);

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
}
