package io.github.jmmedina00.adoolting.service.person.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.sql.Date;
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
public class ConfirmableSelectorTest {
  @MockBean
  private PageService pageService;

  @Autowired
  private ConfirmableSelector selector;

  @Test
  public void getPersonNotificationMapIsEmptyIfIgnored() {
    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setIgnoredAt(new Date(300L));

    Map<Person, Integer> result = selector.getPersonNotificationMap(
      interaction
    );

    assertEquals(Collections.emptyMap(), result);
  }

  public void getPersonNotificationMapIsEmptyIfInteractionIsNotConfirmable() {
    Post post = new Post();

    Map<Person, Integer> result = selector.getPersonNotificationMap(post);

    assertEquals(Collections.emptyMap(), result);
  }

  @Test
  public void getPersonNotificationMapConsistsOfReceiverWithNoCodeWhenNotConfirmed() {
    Person sender = new Person();
    Person receiver = new Person();
    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setInteractor(sender);
    interaction.setReceiverInteractor(receiver);

    Map<Person, Integer> result = selector.getPersonNotificationMap(
      interaction
    );

    assertEquals(Map.of(receiver, 0), result);
  }

  @Test
  public void getPersonNotificationMapConsistsOfSenderWithNoCodeWhenConfirmed() {
    Person sender = new Person();
    Person receiver = new Person();
    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setConfirmedAt(new Date(500L));
    interaction.setInteractor(sender);
    interaction.setReceiverInteractor(receiver);

    Map<Person, Integer> result = selector.getPersonNotificationMap(
      interaction
    );

    assertEquals(Map.of(sender, 0), result);
  }

  @Test
  public void getPersonNotificationMapConsistsOfSenderManagersWhenConfirmed() {
    Page sender = new Page();
    sender.setId(8L);
    Person receiver = new Person();

    Person foo = new Person();
    Person bar = new Person();
    Person baz = new Person();

    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setConfirmedAt(new Date(500L));
    interaction.setInteractor(sender);
    interaction.setReceiverInteractor(receiver);

    Mockito
      .when(pageService.getPageManagers(8L))
      .thenReturn(List.of(foo, bar, baz));
    Map<Person, Integer> result = selector.getPersonNotificationMap(
      interaction
    );

    assertEquals(Map.of(foo, 0, bar, 0, baz, 0), result);
  }

  @Test
  public void getPersonNotificationMapConsistsOfReceiverManagersWhenNotConfirmed() {
    Page receiver = new Page();
    receiver.setId(8L);
    Person sender = new Person();

    Person foo = new Person();
    Person bar = new Person();
    Person baz = new Person();

    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setInteractor(sender);
    interaction.setReceiverInteractor(receiver);

    Mockito
      .when(pageService.getPageManagers(8L))
      .thenReturn(List.of(foo, bar, baz));
    Map<Person, Integer> result = selector.getPersonNotificationMap(
      interaction
    );

    assertEquals(Map.of(foo, 0, bar, 0, baz, 0), result);
  }
}
