package io.github.jmmedina00.adoolting.entity.person.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.enums.Gender;
import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class JoinRequestStrategyTest {
  JoinRequestStrategy strategy = new JoinRequestStrategy();
  Person person;

  @BeforeEach
  public void setUpPerson() {
    person = new Person();
    person.setId(13L);
    person.setFirstName("Juan");
    person.setLastName("Medina");
    person.setEmail("juanmi@test.local");
    person.setGender(Gender.HE);
  }

  @Test
  public void generateDataResultsInArgumentsEquivalentToConfirmableStrategyResultPlusGroupNameWhenNotConfirmed() {
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
    joinRequest.setConfirmedAt(null);

    ConfirmableStrategy parentStrategy = new ConfirmableStrategy();

    EmailData dataFromParent = parentStrategy.generateData(joinRequest, person);
    EmailData data = strategy.generateData(joinRequest, person);

    assertEquals(
      dataFromParent.getSubjectArguments(),
      data.getSubjectArguments().subList(0, 2)
    );
    assertEquals("Group", data.getSubjectArguments().get(2));
  }

  @Test
  public void generateDataResultsInArgumentsEquivalentToConfirmableStrategyResultPlusGroupNameWhenConfirmed() {
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
    joinRequest.setConfirmedAt(new Date());

    ConfirmableStrategy parentStrategy = new ConfirmableStrategy();

    EmailData dataFromParent = parentStrategy.generateData(joinRequest, person);
    EmailData data = strategy.generateData(joinRequest, person);

    assertEquals(
      dataFromParent.getSubjectArguments(),
      data.getSubjectArguments().subList(0, 2)
    );
    assertEquals("Group", data.getSubjectArguments().get(2));
  }

  @Test
  public void generateDataHasInviteAddendumIfRequestNotCreatedByJoiningPerson() {
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

    EmailData data = strategy.generateData(joinRequest, person);
    assertEquals("group.invite", data.getSubjectAddendum());
  }

  @Test
  public void generateDataHasInviteAddendumIfRequestNotCreatedByJoiningPersonInEvent() {
    Page page = new Page();
    page.setName("Page");
    page.setId(18L);

    PeopleGroup group = new PeopleGroup();
    group.setInteractor(page);
    group.setName("Group");

    JoinRequest joinRequest = new JoinRequest();
    joinRequest.setId(18L);
    joinRequest.setInteractor(page);
    joinRequest.setReceiverInteractor(person);
    joinRequest.setGroup(group);

    EmailData data = strategy.generateData(joinRequest, person);
    assertEquals("group.invite", data.getSubjectAddendum());
  }

  @Test
  public void generateDataHasRequestAddendumIfRequestCreatedByJoiningPerson() {
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

    EmailData data = strategy.generateData(joinRequest, person);
    assertEquals("group.request", data.getSubjectAddendum());
  }
}
