package io.github.jmmedina00.adoolting.service.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.group.NewEvent;
import io.github.jmmedina00.adoolting.dto.group.NewGroup;
import io.github.jmmedina00.adoolting.dto.util.SecureDeletion;
import io.github.jmmedina00.adoolting.entity.cache.PersonLocaleConfig;
import io.github.jmmedina00.adoolting.entity.enums.GroupAccessLevel;
import io.github.jmmedina00.adoolting.entity.group.Event;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.group.PeopleGroupRepository;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.cache.PersonLocaleConfigService;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.validation.BindException;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class PeopleGroupServiceTest {
  @MockBean
  private PeopleGroupRepository groupRepository;

  @MockBean
  private PageService pageService;

  @MockBean
  private PersonService personService;

  @MockBean
  private InteractionService interactionService;

  @MockBean
  private InteractorService interactorService;

  @MockBean
  private PersonLocaleConfigService localeConfigService;

  @Autowired
  private PeopleGroupService groupService;

  @Test
  public void getGroupGetsGroupFromRepository() {
    PeopleGroup group = new PeopleGroup();
    Mockito
      .when(groupRepository.findActiveGroup(1L))
      .thenReturn(Optional.of(group));

    PeopleGroup returned = groupService.getGroup(1L);
    assertEquals(group, returned);

    verify(groupRepository, times(1)).findActiveGroup(1L);
  }

  @Test
  public void getGroupThrowsWhenGroupNotFoundInRepository() {
    Mockito
      .when(groupRepository.findActiveGroup(1L))
      .thenReturn(Optional.empty());

    assertThrows(
      Exception.class,
      () -> {
        groupService.getGroup(1L);
      }
    );
  }

  @Test
  public void getGroupManagedByPersonChecksInInteractorServiceBeforeReturningGroup()
    throws NotAuthorizedException {
    PeopleGroup group = new PeopleGroup();
    Person person = new Person();
    person.setId(3L);
    group.setInteractor(person);

    Mockito
      .when(groupRepository.findActiveGroup(1L))
      .thenReturn(Optional.of(group));
    Mockito
      .when(interactorService.getRepresentableInteractorByPerson(3L, 3L))
      .thenReturn(person);

    PeopleGroup returned = groupService.getGroupManagedByPerson(1L, 3L);
    assertEquals(group, returned);

    verify(interactorService, times(1))
      .getRepresentableInteractorByPerson(3L, 3L);
  }

  @Test
  public void getGroupManagedByPersonThrowsIfPersonNotAllowedToRepresentGroupsInteractor()
    throws NotAuthorizedException {
    PeopleGroup group = new PeopleGroup();
    Person person = new Person();
    person.setId(3L);
    group.setInteractor(person);

    Mockito
      .when(groupRepository.findActiveGroup(1L))
      .thenReturn(Optional.of(group));
    Mockito
      .when(interactorService.getRepresentableInteractorByPerson(3L, 3L))
      .thenThrow(NotAuthorizedException.class);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        groupService.getGroupManagedByPerson(1L, 3L);
      }
    );
  }

  @Test
  public void createGroupCreatesGroupWithSpecifiedDetailsAndSavesThroughInteractionService() {
    NewGroup newGroup = new NewGroup();
    newGroup.setName("My group");
    newGroup.setDescription("This is a group");
    newGroup.setAccessLevel(GroupAccessLevel.WATCH_ONLY);

    Person person = new Person();
    Mockito.when(personService.getPerson(1L)).thenReturn(person);
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    PeopleGroup group = groupService.createGroup(newGroup, 1L);

    assertEquals(newGroup.getName(), group.getName());
    assertEquals(newGroup.getDescription(), group.getDescription());
    assertEquals(newGroup.getAccessLevel(), group.getAccessLevel());
    assertEquals(person, group.getInteractor());

    verify(interactionService, times(1)).saveInteraction(group);
    verify(groupRepository, never()).save(group);
  }

  @Test
  public void getGroupsManagedByPersonQueriesRepositoryWhenPersonIdAndPageIdsTheyManage() {
    Person person = new Person();
    person.setId(125L);

    Page foo = new Page();
    foo.setId(98L);
    Page bar = new Page();
    bar.setId(200L);

    Mockito
      .when(pageService.getAllPersonPages(125L))
      .thenReturn(List.of(foo, bar));
    Mockito.when(personService.getPerson(125L)).thenReturn(person);
    Mockito
      .when(groupRepository.findActiveGroupsByInteractorList(any()))
      .thenReturn(List.of());

    groupService.getGroupsManagedByPerson(125L);

    verify(groupRepository, times(1))
      .findActiveGroupsByInteractorList(List.of(98L, 125L, 200L));
  }

  @Test
  public void updateGroupUpdatesGroupWithSpecifiedDetailsAndSavesThroughRepository()
    throws NotAuthorizedException {
    PeopleGroup group = new PeopleGroup();
    Person person = new Person();
    person.setId(1L);
    group.setInteractor(person);

    NewGroup newGroup = new NewGroup();
    newGroup.setName("My group");
    newGroup.setDescription("This is a group");
    newGroup.setAccessLevel(GroupAccessLevel.WATCH_ONLY);

    Mockito
      .when(groupRepository.findActiveGroup(2L))
      .thenReturn(Optional.of(group));
    Mockito
      .when(groupRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    Mockito
      .when(interactorService.getRepresentableInteractorByPerson(1L, 1L))
      .thenReturn(person);

    PeopleGroup updated = groupService.updateGroup(2L, 1L, newGroup);

    assertEquals(newGroup.getName(), updated.getName());
    assertEquals(newGroup.getDescription(), updated.getDescription());
    assertEquals(newGroup.getAccessLevel(), updated.getAccessLevel());

    verify(interactionService, never()).saveInteraction(group);
    verify(groupRepository, times(1)).save(group);
  }

  @Test
  public void getGroupFormReturnsGroupDtoWhenEntityIsAGroup() {
    PeopleGroup group = new PeopleGroup();
    group.setName("My group");
    group.setDescription("This is a group");
    group.setAccessLevel(GroupAccessLevel.WATCH_ONLY);

    Mockito
      .when(groupRepository.findActiveGroup(1L))
      .thenReturn(Optional.of(group));
    NewGroup dto = groupService.getGroupForm(1L);

    assertFalse(dto instanceof NewEvent);
    assertEquals(group.getName(), dto.getName());
    assertEquals(group.getDescription(), dto.getDescription());
    assertEquals(group.getAccessLevel(), dto.getAccessLevel());
  }

  @Test
  public void getGroupFormReturnsEventDtoWhenEntityIsAEvent() {
    Event event = new Event();
    Person person = new Person();
    person.setId(4L);
    event.setName("My group");
    event.setDescription("This is a group");
    event.setAccessLevel(GroupAccessLevel.WATCH_ONLY);
    event.setLocation("Somewhere");
    event.setHappeningAt(new Date());
    event.setInteractor(person);

    PersonLocaleConfig config = new PersonLocaleConfig();
    config.setOffsetFromUTC(120);

    MockedStatic<AuthenticatedPerson> authPersonUtilities = Mockito.mockStatic(
      AuthenticatedPerson.class
    );
    authPersonUtilities.when(AuthenticatedPerson::getPersonId).thenReturn(4L);
    Mockito.when(localeConfigService.getConfig(4L)).thenReturn(config);

    Mockito
      .when(groupRepository.findActiveGroup(1L))
      .thenReturn(Optional.of(event));
    NewEvent dto = (NewEvent) groupService.getGroupForm(1L);

    assertEquals(event.getName(), dto.getName());
    assertEquals(event.getDescription(), dto.getDescription());
    assertEquals(event.getAccessLevel(), dto.getAccessLevel());
    assertEquals(event.getLocation(), dto.getLocation());
    assertEquals(4L, dto.getCreateAs());

    authPersonUtilities.closeOnDemand();
  }

  @Test
  public void getGroupFormSetsDateAndTimeCorrectlyInEventDto() {
    long savedTimestamp = 1584026760_000L; // 2020/03/12 at 15:26 UTC
    Date resultingDate = new Date(savedTimestamp); // Gets converted to local timezone (CET -> 16:26)

    Event event = new Event();
    Person person = new Person();
    person.setId(4L);
    event.setName("My group");
    event.setDescription("This is a group");
    event.setAccessLevel(GroupAccessLevel.WATCH_ONLY);
    event.setLocation("Somewhere");
    event.setHappeningAt(resultingDate);
    event.setInteractor(person);

    PersonLocaleConfig config = new PersonLocaleConfig();
    config.setOffsetFromUTC(120);

    MockedStatic<AuthenticatedPerson> authPersonUtilities = Mockito.mockStatic(
      AuthenticatedPerson.class
    );
    authPersonUtilities.when(AuthenticatedPerson::getPersonId).thenReturn(4L);
    Mockito.when(localeConfigService.getConfig(4L)).thenReturn(config);

    Mockito
      .when(groupRepository.findActiveGroup(1L))
      .thenReturn(Optional.of(event));
    NewEvent dto = (NewEvent) groupService.getGroupForm(1L);
    authPersonUtilities.closeOnDemand();

    assertEquals("2020-03-12", dto.getDate().toString());
    assertEquals("13:26", dto.getTime().toString());
  }

  @Test
  public void deleteGroupSetsGroupAsDeleted() throws Exception {
    PeopleGroup group = new PeopleGroup();
    Person person = new Person();
    person.setId(5L);
    group.setInteractor(person);

    Mockito
      .when(groupRepository.findActiveGroup(10L))
      .thenReturn(Optional.of(group));
    Mockito
      .when(personService.getPersonWithMatchingPassword(eq(5L), any()))
      .thenReturn(person);
    Mockito
      .when(interactorService.getRepresentableInteractorByPerson(5L, 5L))
      .thenReturn(person);
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    PeopleGroup deleted = groupService.deleteGroup(
      10L,
      5L,
      new SecureDeletion()
    );
    assertNotNull(deleted.getDeletedAt());

    verify(interactionService, times(1)).saveInteraction(group);
    verify(groupRepository, never()).save(group);
  }

  @Test
  public void deleteGroupThrowsExceptionCreatedByPersonService()
    throws Exception {
    Mockito
      .when(personService.getPersonWithMatchingPassword(eq(5L), any()))
      .thenThrow(new BindException(new NewGroup(), "test"));

    assertThrows(
      BindException.class,
      () -> {
        groupService.deleteGroup(10L, 5L, new SecureDeletion());
      }
    );
  }
}
