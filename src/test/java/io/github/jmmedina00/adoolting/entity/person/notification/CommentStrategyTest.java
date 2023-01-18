package io.github.jmmedina00.adoolting.entity.person.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.enums.Gender;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class CommentStrategyTest {
  CommentStrategy strategy = new CommentStrategy();
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
  public void generateDataSetsArgumentsToCommenterThenOriginalInteractorName() {
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

    EmailData data = strategy.generateData(comment, person);
    assertEquals(
      List.of("Maria Hernandez", "Test Page"),
      data.getSubjectArguments()
    );
    assertEquals("page", data.getSubjectAddendum());
  }

  @Test
  public void generateDataSetsArgumentsToCommenterThenOriginalInteractorsNames() {
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

    EmailData data = strategy.generateData(comment, person);
    assertEquals(
      List.of("Maria Hernandez", "Alpha", "Bravo"),
      data.getSubjectArguments()
    );
    assertEquals("page", data.getSubjectAddendum());
  }

  @Test
  public void generateDataContinuesSettingAddendumToProfileIfPersonIsDirectlyInvolved() {
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

    EmailData data = strategy.generateData(comment, person);
    assertEquals(
      List.of("Page", "Juan Medina", "Page"),
      data.getSubjectArguments()
    );
    assertEquals("profile", data.getSubjectAddendum());
  }
}
