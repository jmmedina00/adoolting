package io.github.jmmedina00.adoolting.entity.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.enums.Gender;
import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class NotificationTest {
  Person person;
  Notification notification;

  @BeforeEach
  public void setUpPerson() {
    person = new Person();
    person.setId(13L);
    person.setFirstName("Juan");
    person.setLastName("Medina");
    person.setEmail("juanmi@test.local");
    person.setGender(Gender.HE);

    notification = new Notification();
    notification.setForPerson(person);
  }

  @Test
  public void getEmailDataPopulatesPersonAndParametersWithMinimumInformation() {
    Person creator = Mockito.mock(Person.class);
    Mockito.when(creator.getFullName()).thenReturn("Maria Hernandez");
    Post post = new Post();
    post.setId(12L);
    post.setInteractor(creator);

    notification.setInteraction(post);

    EmailData data = notification.getEmailData();
    assertEquals(person.getId(), data.getPerson().getId());
    assertEquals(Map.of("interaction", "12"), data.getParameters());
  }

  @Test
  public void getEmailDataPopulatesInteractorNameIntoArguments() {
    Page page = new Page();
    page.setName("Test Page");

    Post post = new Post();
    post.setId(12L);
    post.setInteractor(page);

    notification.setInteraction(post);
    EmailData data = notification.getEmailData();
    assertEquals(List.of("Test Page"), data.getSubjectArguments());
  }

  @Test
  public void getEmailDataPopulatesInteractorInvolvedNamesIntoArguments() {
    Page foo = new Page();
    foo.setName("Alpha");
    Page bar = new Page();
    bar.setName("Bravo");

    Post post = new Post();
    post.setId(12L);
    post.setInteractor(foo);
    post.setReceiverInteractor(bar);

    notification.setInteraction(post);
    EmailData data = notification.getEmailData();
    assertEquals(List.of("Alpha", "Bravo"), data.getSubjectArguments());
  }

  @Test
  public void getEmailDataAppendsOriginalInteractionCreatorIfInteractionIsAComment() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");

    Page page = new Page();
    page.setName("Test Page");

    Post post = new Post();
    post.setId(12L);
    post.setInteractor(page);

    Comment comment = new Comment();
    comment.setId(14L);
    comment.setInteractor(commenter);
    comment.setReceiverInteraction(post);

    notification.setInteraction(comment);
    EmailData data = notification.getEmailData();
    assertEquals(
      List.of("Maria Hernandez", "Test Page"),
      data.getSubjectArguments()
    );
  }

  @Test
  public void getEmailDataAppendsOriginalInteractionInvolvedInteractorsIfInteractionIsAComment() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");

    Page foo = new Page();
    foo.setName("Alpha");
    Page bar = new Page();
    bar.setName("Bravo");

    Post post = new Post();
    post.setId(12L);
    post.setInteractor(foo);
    post.setReceiverInteractor(bar);

    Comment comment = new Comment();
    comment.setId(14L);
    comment.setInteractor(commenter);
    comment.setReceiverInteraction(post);

    notification.setInteraction(comment);
    EmailData data = notification.getEmailData();
    assertEquals(
      List.of("Maria Hernandez", "Alpha", "Bravo"),
      data.getSubjectArguments()
    );
  }

  @Test
  public void getEmailDataHasPageAddendumIfReceiverPersonIsNotInvolved() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");

    Page page = new Page();
    page.setName("Page");

    Post post = new Post();
    post.setId(12L);
    post.setInteractor(commenter);
    post.setReceiverInteractor(page);

    notification.setInteraction(post);
    EmailData data = notification.getEmailData();
    assertEquals("page", data.getSubjectAddendum());
  }

  @Test
  public void getEmailDataHasPageAddendumIfReceiverPersonIsNotInvolvedInComment() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");

    Page page = new Page();
    page.setName("Page");

    Post post = new Post();
    post.setId(12L);
    post.setInteractor(page);

    Comment comment = new Comment();
    comment.setId(14L);
    comment.setInteractor(commenter);
    comment.setReceiverInteraction(post);

    notification.setInteraction(comment);
    EmailData data = notification.getEmailData();
    assertEquals("page", data.getSubjectAddendum());
  }

  @Test
  public void getEmailDataHasProfileAddendumIfReceiverPersonIsInvolved() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");

    Post post = new Post();
    post.setId(12L);
    post.setInteractor(commenter);
    post.setReceiverInteractor(person);

    notification.setInteraction(post);
    EmailData data = notification.getEmailData();
    assertEquals("profile", data.getSubjectAddendum());
  }

  @Test
  public void getEmailDataHasProfileAddendumIfReceiverPersonIsInvolvedInComment() {
    Page page = new Page();
    page.setName("Page");

    Post post = new Post();
    post.setId(12L);
    post.setInteractor(person);
    post.setReceiverInteractor(page);

    Comment comment = new Comment();
    comment.setId(14L);
    comment.setInteractor(page);
    comment.setReceiverInteraction(post);

    notification.setInteraction(comment);
    EmailData data = notification.getEmailData();
    assertEquals("profile", data.getSubjectAddendum());
  }

  @Test
  public void getEmailDataHasFriendAddendumIfInteractionIsConfirmable() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");

    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setId(18L);
    interaction.setConfirmedAt(null);
    interaction.setInteractor(commenter);
    interaction.setReceiverInteractor(person);

    notification.setInteraction(interaction);
    EmailData data = notification.getEmailData();
    assertEquals("friend", data.getSubjectAddendum());
    assertEquals(
      List.of("Maria Hernandez", "Juan Medina"),
      data.getSubjectArguments()
    );
  }

  @Test
  public void getEmailDataReversesArgumentsOrderIfConfirmableInteractionIsAccepted() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");

    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setId(18L);
    interaction.setConfirmedAt(new Date(500));
    interaction.setInteractor(person);
    interaction.setReceiverInteractor(commenter);

    notification.setInteraction(interaction);
    EmailData data = notification.getEmailData();
    assertEquals("friend", data.getSubjectAddendum());
    assertEquals(
      List.of("Maria Hernandez", "Juan Medina"),
      data.getSubjectArguments()
    );
  }

  @Test
  public void getEmailDataAddsGroupAndToArgumentsAndHasInviteAddendumIfInteractionIsJoinRequestCreatedByGroupCreator() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");
    Mockito.when(commenter.getId()).thenReturn(2L);

    PeopleGroup group = new PeopleGroup();
    group.setInteractor(commenter);
    group.setName("Group");

    JoinRequest joinRequest = new JoinRequest();
    joinRequest.setId(18L);
    joinRequest.setInteractor(commenter);
    joinRequest.setReceiverInteractor(person);
    joinRequest.setGroup(group);

    notification.setInteraction(joinRequest);
    EmailData data = notification.getEmailData();
    assertEquals("group.invite", data.getSubjectAddendum());
    assertEquals(3, data.getSubjectArguments().size());
    assertEquals("Group", data.getSubjectArguments().get(2));
  }

  @Test
  public void getEmailDataAddsGroupAndToArgumentsAndHasInviteAddendumIfInteractionIsJoinRequestCreatedByJoiningPerson() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");
    Mockito.when(commenter.getId()).thenReturn(2L);

    PeopleGroup group = new PeopleGroup();
    group.setInteractor(commenter);
    group.setName("Group");

    JoinRequest joinRequest = new JoinRequest();
    joinRequest.setId(18L);
    joinRequest.setInteractor(person);
    joinRequest.setReceiverInteractor(commenter);
    joinRequest.setGroup(group);

    notification.setInteraction(joinRequest);
    EmailData data = notification.getEmailData();
    assertEquals("group.request", data.getSubjectAddendum());
    assertEquals(3, data.getSubjectArguments().size());
    assertEquals("Group", data.getSubjectArguments().get(2));
  }
}
