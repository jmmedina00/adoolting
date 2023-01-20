package io.github.jmmedina00.adoolting.service.person.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;

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
public class PageFollowersSelectorTest {
  @MockBean
  private PageService pageService;

  @MockBean
  private PersonService personService;

  @Autowired
  private PageFollowersSelector selector;

  @Test
  public void getPersonNotificationMapAddsPeopleWhoLikedPage() {
    Post post = new Post();
    Page page = new Page();
    page.setId(7L);
    post.setInteractor(page);

    Person foo = new Person();
    Person bar = new Person();
    Person baz = new Person();

    Mockito
      .when(personService.getPersonsWhoLikedPage(7L))
      .thenReturn(List.of(foo, bar, baz));
    Map<Person, Integer> result = selector.getPersonNotificationMap(post);

    assertEquals(
      Map.of(
        foo,
        PersonSettingsService.NOTIFY_PAGE_INTERACTION,
        bar,
        PersonSettingsService.NOTIFY_PAGE_INTERACTION,
        baz,
        PersonSettingsService.NOTIFY_PAGE_INTERACTION
      ),
      result
    );
  }

  @Test
  public void getPersonNotificationMapIsEmptyIfAuthorIsNotAPage() {
    Post post = new Post();
    Person person = new Person();
    person.setId(7L);
    post.setInteractor(person);

    Person foo = new Person();
    Person bar = new Person();
    Person baz = new Person();

    Mockito
      .when(personService.getPersonsWhoLikedPage(anyLong()))
      .thenReturn(List.of(foo, bar, baz));
    Map<Person, Integer> result = selector.getPersonNotificationMap(post);

    assertEquals(Collections.emptyMap(), result);
  }
}
