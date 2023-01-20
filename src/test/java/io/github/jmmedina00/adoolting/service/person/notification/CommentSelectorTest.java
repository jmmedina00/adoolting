package io.github.jmmedina00.adoolting.service.person.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.PersonSettingsService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
public class CommentSelectorTest {
  @MockBean
  private PageService pageService;

  @Autowired
  private CommentSelector selector;

  @Test
  public void getPersonNotificationMapConsistsOfOriginalInteractionCreatorWithCommentCode() {
    Post post = new Post();
    Person person = new Person();
    person.setId(4L);
    post.setInteractor(person);

    Comment comment = new Comment();
    comment.setReceiverInteraction(post);
    Person creator = new Person();
    creator.setId(5L);
    comment.setInteractor(creator);

    Map<Person, Integer> result = selector.getPersonNotificationMap(comment);

    assertEquals(Map.of(person, PersonSettingsService.NOTIFY_COMMENT), result);
  }

  @Test
  public void getPersonNotificationMapConsistsOfOriginalInteractionCreatorManagers() {
    Post post = new Post();
    Page page = new Page();
    page.setId(4L);
    post.setInteractor(page);

    Comment comment = new Comment();
    comment.setReceiverInteraction(post);
    Person creator = new Person();
    creator.setId(5L);
    comment.setInteractor(creator);

    Person foo = new Person();
    Person bar = new Person();

    Mockito.when(pageService.getPageManagers(4L)).thenReturn(List.of(foo, bar));
    Map<Person, Integer> result = selector.getPersonNotificationMap(comment);

    assertEquals(
      Map.of(
        foo,
        PersonSettingsService.NOTIFY_COMMENT,
        bar,
        PersonSettingsService.NOTIFY_COMMENT
      ),
      result
    );
  }

  @Test
  public void getPersonNotificationMapIsEmptyWhenInteractionCreatorAndCommentCreatorMatch() {
    Post post = new Post();
    Person person = new Person();
    person.setId(4L);
    post.setInteractor(person);

    Comment comment = new Comment();
    comment.setReceiverInteraction(post);
    comment.setInteractor(person);

    Map<Person, Integer> result = selector.getPersonNotificationMap(comment);

    assertEquals(Collections.emptyMap(), result);
  }

  @Test
  public void getPersonNotificationMapIsEmptyWhenParameterIsNotAComment() {
    Post post = new Post();

    Map<Person, Integer> result = selector.getPersonNotificationMap(post);

    assertEquals(Collections.emptyMap(), result);
  }
}
