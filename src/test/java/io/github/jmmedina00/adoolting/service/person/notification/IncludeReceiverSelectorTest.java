package io.github.jmmedina00.adoolting.service.person.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
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
public class IncludeReceiverSelectorTest {
  @MockBean
  private PageService pageService;

  @MockBean
  private PersonService personService;

  @Autowired
  private IncludeReceiverSelector selector;

  @Test
  public void getPersonNotificationMapConsistsOfReceiver() {
    Post post = new Post();
    Person sender = new Person();
    Person receiver = new Person();

    post.setInteractor(sender);
    post.setReceiverInteractor(receiver);

    Map<Person, Integer> result = selector.getPersonNotificationMap(post);
    assertEquals(
      Map.of(receiver, PersonSettingsService.NOTIFY_POST_FROM_OTHER),
      result
    );
  }

  @Test
  public void getPersonNotificationMapConsistsOfReceiverManagers() {
    Post post = new Post();
    Person sender = new Person();
    Page receiver = new Page();
    receiver.setId(5L);

    Person foo = new Person();
    Person bar = new Person();

    post.setInteractor(sender);
    post.setReceiverInteractor(receiver);

    Mockito.when(pageService.getPageManagers(5L)).thenReturn(List.of(foo, bar));
    Map<Person, Integer> result = selector.getPersonNotificationMap(post);
    assertEquals(
      Map.of(
        foo,
        PersonSettingsService.NOTIFY_POST_FROM_OTHER,
        bar,
        PersonSettingsService.NOTIFY_POST_FROM_OTHER
      ),
      result
    );
  }

  @Test
  public void getPersonNotificationMapIsEmptyIfInteractionHasNoReceiver() {
    Person person = new Person();
    person.setId(4L);
    Post post = new Post();
    post.setInteractor(person);

    Map<Person, Integer> result = selector.getPersonNotificationMap(post);
    assertEquals(Collections.emptyMap(), result);
  }
}
