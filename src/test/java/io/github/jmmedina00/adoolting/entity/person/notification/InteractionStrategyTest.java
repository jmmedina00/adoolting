package io.github.jmmedina00.adoolting.entity.person.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.enums.Gender;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
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
public class InteractionStrategyTest {
  InteractionStrategy strategy = new InteractionStrategy();
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
  public void generateDataPopulatesPersonAndParametersWithMinimumInformation() {
    Person creator = Mockito.mock(Person.class);
    Mockito.when(creator.getFullName()).thenReturn("Maria Hernandez");
    Post post = new Post();
    post.setId(12L);
    post.setInteractor(creator);

    EmailData data = strategy.generateData(post, person);
    assertEquals(person.getId(), data.getPerson().getId());
    assertEquals(Map.of("interaction", "12"), data.getParameters());
  }

  @Test
  public void generateDataPopulatesInteractorNameIntoArguments() {
    Page page = new Page();
    page.setName("Test Page");

    Post post = new Post();
    post.setId(12L);
    post.setInteractor(page);

    EmailData data = strategy.generateData(post, person);
    assertEquals(List.of("Test Page"), data.getSubjectArguments());
  }

  @Test
  public void generateDataPopulatesInteractorInvolvedNamesIntoArguments() {
    Page foo = new Page();
    foo.setName("Alpha");
    Page bar = new Page();
    bar.setName("Bravo");

    Post post = new Post();
    post.setId(12L);
    post.setInteractor(foo);
    post.setReceiverInteractor(bar);

    EmailData data = strategy.generateData(post, person);
    assertEquals(List.of("Alpha", "Bravo"), data.getSubjectArguments());
  }

  @Test
  public void generateDataHasPageAddendumIfReceiverPersonIsNotInvolved() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");

    Page page = new Page();
    page.setName("Page");

    Post post = new Post();
    post.setId(12L);
    post.setInteractor(commenter);
    post.setReceiverInteractor(page);

    EmailData data = strategy.generateData(post, person);
    assertEquals("page", data.getSubjectAddendum());
  }

  @Test
  public void generateDataHasProfileAddendumIfReceiverPersonIsInvolved() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");

    Post post = new Post();
    post.setId(12L);
    post.setInteractor(commenter);
    post.setReceiverInteractor(person);

    EmailData data = strategy.generateData(post, person);
    assertEquals("profile", data.getSubjectAddendum());
  }
}
