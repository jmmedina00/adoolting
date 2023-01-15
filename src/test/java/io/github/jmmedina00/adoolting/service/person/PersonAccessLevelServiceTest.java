package io.github.jmmedina00.adoolting.service.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.enums.AccessLevel;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.group.JoinRequestService;
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
public class PersonAccessLevelServiceTest {
  @MockBean
  private InteractorService interactorService;

  @MockBean
  private InteractionService interactionService;

  @MockBean
  private ConfirmableInteractionService cInteractionService;

  @MockBean
  private PersonSettingsService settingsService;

  @MockBean
  private JoinRequestService joinRequestService;

  @Autowired
  private PersonAccessLevelService accessLevelService;

  @Test
  public void getAccessLevelThatPersonHasOnInteractorReturnsOpenIfOtherInteractorIsAPage() {
    Page page = new Page();

    Mockito.when(interactorService.getInteractor(12L)).thenReturn(page);

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteractor(
      4L,
      12L
    );
    assertEquals(AccessLevel.OPEN, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractorReturnsOpenIfOtherInteractorIsAPersonRequestingPersonIsFriendsWith() {
    Person person = new Person();
    ConfirmableInteraction interaction = new ConfirmableInteraction();

    Mockito.when(interactorService.getInteractor(12L)).thenReturn(person);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(interaction);

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteractor(
      4L,
      12L
    );
    assertEquals(AccessLevel.OPEN, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractorReturnsClosedIfOtherInteractorIsANonFriendWhoWontAllowStrangersIntoTheirProfile() {
    Person person = new Person();

    Mockito.when(interactorService.getInteractor(12L)).thenReturn(person);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(false);

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteractor(
      4L,
      12L
    );
    assertEquals(AccessLevel.CLOSED, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractorReturnsOpenIfPersonQueryingAgainstThemselves() {
    Person person = new Person();

    Mockito.when(interactorService.getInteractor(4L)).thenReturn(person);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 4L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          4L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(false);

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteractor(
      4L,
      4L
    );
    assertEquals(AccessLevel.OPEN, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractorReturnsWatchOnlyIfOtherInteractorIsNotFriendsWithPersonAndOnlyAllowsOthersToWatchTheirProfile() {
    Person person = new Person();

    Mockito.when(interactorService.getInteractor(12L)).thenReturn(person);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(true);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.COMMENT_ON_INTERACTION
        )
      )
      .thenReturn(false);

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteractor(
      4L,
      12L
    );
    assertEquals(AccessLevel.WATCH_ONLY, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractorReturnsOpenIfOtherInteractorIsNotFriendsWithPersonButAllowsAnyoneToCommentOnTheirProfile() {
    Person person = new Person();

    Mockito.when(interactorService.getInteractor(12L)).thenReturn(person);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(true);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.COMMENT_ON_INTERACTION
        )
      )
      .thenReturn(true);

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteractor(
      4L,
      12L
    );
    assertEquals(AccessLevel.OPEN, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractionReturnsTheSameLevelOfAccessAsTheCreatorOfTheInteraction() {
    Person person = new Person();
    person.setId(12L);
    Post post = new Post();
    post.setInteractor(person);

    Mockito.when(interactionService.getInteraction(25L)).thenReturn(post);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(true);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.COMMENT_ON_INTERACTION
        )
      )
      .thenReturn(false); // Should return WATCH_ONLY for interactor

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      4L,
      25L
    );
    assertEquals(AccessLevel.WATCH_ONLY, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractionReturnsGroupAccessLevelAsIsForStrangers() {
    PeopleGroup group = new PeopleGroup();
    group.setAccessLevel(AccessLevel.CLOSED);

    Mockito.when(interactionService.getInteraction(25L)).thenReturn(group);
    Mockito.when(joinRequestService.isMemberOfGroup(25L, 4L)).thenReturn(false);

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      4L,
      25L
    );
    assertEquals(AccessLevel.CLOSED, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractionReturnsOpenIfPersonIsMemberOfTheGroup() {
    PeopleGroup group = new PeopleGroup();
    group.setId(25L);
    group.setAccessLevel(AccessLevel.CLOSED);

    Mockito.when(interactionService.getInteraction(25L)).thenReturn(group);
    Mockito.when(joinRequestService.isMemberOfGroup(25L, 4L)).thenReturn(true);

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      4L,
      25L
    );
    assertEquals(AccessLevel.OPEN, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractionReturnsCorrespondingAccessLevelOfCommentedInteraction() {
    PeopleGroup group = new PeopleGroup();
    group.setId(17L);
    group.setAccessLevel(AccessLevel.CLOSED);
    Comment comment = new Comment();
    comment.setReceiverInteraction(group);

    Mockito.when(interactionService.getInteraction(17L)).thenReturn(comment);
    Mockito.when(joinRequestService.isMemberOfGroup(25L, 4L)).thenReturn(false);

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      4L,
      17L
    );
    assertEquals(AccessLevel.CLOSED, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractionReturnsOpenIfBothInteractorsYieldOpen() {
    Person person = new Person();
    person.setId(12L);
    Page page = new Page();
    page.setId(13L);

    Post post = new Post();
    post.setInteractor(person);
    post.setReceiverInteractor(page);

    ConfirmableInteraction interaction = new ConfirmableInteraction();

    Mockito.when(interactionService.getInteraction(25L)).thenReturn(post);
    Mockito.when(interactorService.getInteractor(12L)).thenReturn(person);
    Mockito.when(interactorService.getInteractor(13L)).thenReturn(page);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(interaction); // Equals OPEN for Person

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      4L,
      25L
    );
    assertEquals(AccessLevel.OPEN, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractionReturnsWatchOnlyIfOneIsOpenButTheOtherIsClosed() {
    Person person = new Person();
    person.setId(12L);
    Page page = new Page();
    page.setId(13L);

    Post post = new Post();
    post.setInteractor(person);
    post.setReceiverInteractor(page);

    Mockito.when(interactionService.getInteraction(25L)).thenReturn(post);
    Mockito.when(interactorService.getInteractor(12L)).thenReturn(person);
    Mockito.when(interactorService.getInteractor(13L)).thenReturn(page);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(false); // Equals CLOSED for Person

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      4L,
      25L
    );
    assertEquals(AccessLevel.WATCH_ONLY, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractionReturnsOpenIfRequestingForPersonDirectlyInvolvedInInteraction() {
    Person person = new Person();
    person.setId(12L);
    Page page = new Page();
    page.setId(13L);

    Post post = new Post();
    post.setInteractor(person);
    post.setReceiverInteractor(page);

    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(12L, 12L))
      .thenReturn(true);
    Mockito.when(interactionService.getInteraction(25L)).thenReturn(post);
    Mockito.when(interactorService.getInteractor(12L)).thenReturn(person);
    Mockito.when(interactorService.getInteractor(13L)).thenReturn(page);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(false); // Equals CLOSED for Person

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      12L,
      25L
    );
    assertEquals(AccessLevel.OPEN, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractionReturnsOpenIfPageInvolvedIsManagedByPersonRequesting() {
    Person person = new Person();
    person.setId(12L);
    Page page = new Page();
    page.setId(13L);

    Post post = new Post();
    post.setInteractor(person);
    post.setReceiverInteractor(page);

    Mockito
      .when(interactorService.isInteractorRepresentableByPerson(13L, 4L))
      .thenReturn(true);
    Mockito.when(interactionService.getInteraction(25L)).thenReturn(post);
    Mockito.when(interactorService.getInteractor(12L)).thenReturn(person);
    Mockito.when(interactorService.getInteractor(13L)).thenReturn(page);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(false); // Equals CLOSED for Person

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      4L,
      25L
    );
    assertEquals(AccessLevel.OPEN, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractionReturnsWatchOnlyIfOneIsOpenButTheOtherIsWatchOnly() {
    Person person = new Person();
    person.setId(12L);
    Page page = new Page();
    page.setId(13L);

    Post post = new Post();
    post.setInteractor(person);
    post.setReceiverInteractor(page);

    Mockito.when(interactionService.getInteraction(25L)).thenReturn(post);
    Mockito.when(interactorService.getInteractor(12L)).thenReturn(person);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(true);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.COMMENT_ON_INTERACTION
        )
      )
      .thenReturn(false); // Equals WATCH_ONLY for person

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      4L,
      25L
    );
    assertEquals(AccessLevel.WATCH_ONLY, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractionReturnsWatchOnlyIfBothIOnteractorsYieldWatchOnly() {
    Person foo = new Person();
    foo.setId(12L);
    Person bar = new Person();
    bar.setId(13L);

    Post post = new Post();
    post.setInteractor(foo);
    post.setReceiverInteractor(bar);

    Mockito.when(interactionService.getInteraction(25L)).thenReturn(post);
    Mockito.when(interactorService.getInteractor(12L)).thenReturn(foo);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(true);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.COMMENT_ON_INTERACTION
        )
      )
      .thenReturn(false); // Equals WATCH_ONLY for person

    Mockito.when(interactorService.getInteractor(13L)).thenReturn(bar);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 13L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          13L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(true);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          13L,
          PersonSettingsService.COMMENT_ON_INTERACTION
        )
      )
      .thenReturn(false); // Equals WATCH_ONLY for person

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      4L,
      25L
    );
    assertEquals(AccessLevel.WATCH_ONLY, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractionReturnsWatchOnlyIfOneYieldsWatchOnlyAndTheOtherYieldsClosed() {
    Person foo = new Person();
    foo.setId(12L);
    Person bar = new Person();
    bar.setId(13L);

    Post post = new Post();
    post.setInteractor(foo);
    post.setReceiverInteractor(bar);

    Mockito.when(interactionService.getInteraction(25L)).thenReturn(post);
    Mockito.when(interactorService.getInteractor(12L)).thenReturn(foo);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(true);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.COMMENT_ON_INTERACTION
        )
      )
      .thenReturn(false); // Equals WATCH_ONLY for person

    Mockito.when(interactorService.getInteractor(13L)).thenReturn(bar);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 13L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          13L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(false); // Equals CLOSED for person

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      4L,
      25L
    );
    assertEquals(AccessLevel.WATCH_ONLY, level);
  }

  @Test
  public void getAccessLevelThatPersonHasOnInteractionReturnsClosedIfBothInteractorsYieldClosed() {
    Person foo = new Person();
    foo.setId(12L);
    Person bar = new Person();
    bar.setId(13L);

    Post post = new Post();
    post.setInteractor(foo);
    post.setReceiverInteractor(bar);

    Mockito.when(interactionService.getInteraction(25L)).thenReturn(post);
    Mockito.when(interactorService.getInteractor(12L)).thenReturn(foo);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 12L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          12L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(false); // Equals CLOSED for person

    Mockito.when(interactorService.getInteractor(13L)).thenReturn(bar);
    Mockito
      .when(cInteractionService.getPersonFriendship(4L, 13L))
      .thenReturn(null);
    Mockito
      .when(
        settingsService.isAllowedByPerson(
          13L,
          PersonSettingsService.ENTER_PROFILE
        )
      )
      .thenReturn(false); // Equals CLOSED for person

    AccessLevel level = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      4L,
      25L
    );
    assertEquals(AccessLevel.CLOSED, level);
  }
}
