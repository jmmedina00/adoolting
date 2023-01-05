package io.github.jmmedina00.adoolting.service.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class NotifiedInteractorServiceTest {
  @MockBean
  private PersonService personService;

  @MockBean
  private PageService pageService;

  @Autowired
  private NotifiedInteractorService notifiedInteractorService;

  @Test
  public void getInteractorsInterestedInInteractionReturnsNothingIfTheInteractionWasDeleted() {
    Post post = new Post();
    post.setDeletedAt(new Date(200L));

    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      post
    );

    assertEquals(Collections.emptyMap(), result);
  }

  @Test
  public void getInteractorsInterestedInInteractionReturnsNothingIfConfirmableInteractionIgnored() {
    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setIgnoredAt(new Date(300L));

    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      interaction
    );

    assertEquals(Collections.emptyMap(), result);
  }

  @Test
  public void getInteractorsInterestedInInteractionReturnsOnlyReceiverWhenConfirmableInteractionIsNotConfirmed() {
    Person sender = new Person();
    Person receiver = new Person();
    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setInteractor(sender);
    interaction.setReceiverInteractor(receiver);

    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      interaction
    );

    assertEquals(Map.of(receiver, 0), result);
  }

  @Test
  public void getInteractorsInterestedInInteractionReturnsOnlySenderWhenConfirmableInteractionIsConfirmed() {
    Person sender = new Person();
    Person receiver = new Person();
    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setConfirmedAt(new Date(500L));
    interaction.setInteractor(sender);
    interaction.setReceiverInteractor(receiver);

    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      interaction
    );

    assertEquals(Map.of(sender, 0), result);
  }

  @Test
  public void getInteractorsInterestedInInteractionReturnsOnlySenderManagersWhenConfirmableInteractionIsConfirmed() {
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
    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      interaction
    );

    assertEquals(Map.of(foo, 0, bar, 0, baz, 0), result);
  }

  @Test
  public void getInteractorsInterestedInInteractionNotifiesOriginalInteractionCreatorWhenItIsComment() {
    Post post = new Post();
    Person person = new Person();
    person.setId(4L);
    post.setInteractor(person);

    Comment comment = new Comment();
    comment.setReceiverInteraction(post);
    Person creator = new Person();
    creator.setId(5L);
    comment.setInteractor(creator);

    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      comment
    );

    assertEquals(Map.of(person, PersonSettingsService.NOTIFY_COMMENT), result);
  }

  @Test
  public void getInteractorsInterestedInInteractionNotifiesOriginalInteractionCreatorManagersWhenItIsComment() {
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
    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      comment
    );

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
  public void getInteractorsInterestedInInteractionSkipsCommentCreatorIfTheyAreCommentingInTheirOwnInteraction() {
    Post post = new Post();
    Person person = new Person();
    person.setId(4L);
    post.setInteractor(person);

    Comment comment = new Comment();
    comment.setReceiverInteraction(post);
    comment.setInteractor(person);

    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      comment
    );

    assertEquals(Collections.emptyMap(), result);
  }

  @Test
  public void getInteractorsInterestedInInteractionAddsPeopleWhoLikedPageIfInteractorIsAPage() {
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
    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      post
    );

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
  public void getInteractorsInterestedInInteractionAddsInteractionReceiverIfAny() {
    Post post = new Post();
    Person sender = new Person();
    Person receiver = new Person();

    post.setInteractor(sender);
    post.setReceiverInteractor(receiver);

    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      post
    );
    assertEquals(
      Map.of(receiver, PersonSettingsService.NOTIFY_POST_FROM_OTHER),
      result
    );
  }

  @Test
  public void getInteractorsInterestedInInteractionAddsInteractionReceiverManagersIfAny() {
    Post post = new Post();
    Person sender = new Person();
    Page receiver = new Page();
    receiver.setId(5L);

    Person foo = new Person();
    Person bar = new Person();

    post.setInteractor(sender);
    post.setReceiverInteractor(receiver);

    Mockito.when(pageService.getPageManagers(5L)).thenReturn(List.of(foo, bar));
    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      post
    );
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
  public void getInteractorsInterestedInInteractionGivesReceiverCodeHigherPriorityThanLikeCode() {
    Person foo = new Person();
    Person bar = new Person();
    Person baz = new Person();

    Page sender = new Page();
    sender.setId(5L);
    Page receiver = new Page();
    receiver.setId(6L);

    Post post = new Post();
    post.setInteractor(sender);
    post.setReceiverInteractor(receiver);

    Mockito
      .when(personService.getPersonsWhoLikedPage(5L))
      .thenReturn(List.of(foo, bar));
    Mockito.when(pageService.getPageManagers(6L)).thenReturn(List.of(bar, baz));

    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      post
    );

    assertEquals(
      Map.of(
        foo,
        PersonSettingsService.NOTIFY_PAGE_INTERACTION,
        bar,
        PersonSettingsService.NOTIFY_POST_FROM_OTHER,
        baz,
        PersonSettingsService.NOTIFY_POST_FROM_OTHER
      ),
      result
    );
  }

  @Test
  public void getInteractorsInterestedInInteractionGivesCommentCodeHigherPriorityThanLikeCode() {
    Person foo = new Person();
    Person bar = new Person();
    Person baz = new Person();

    Page sender = new Page();
    sender.setId(5L);
    Page receiver = new Page();
    receiver.setId(6L);

    Post post = new Post();
    post.setInteractor(receiver);
    Comment comment = new Comment();
    comment.setInteractor(sender);
    comment.setReceiverInteraction(post);

    Mockito
      .when(personService.getPersonsWhoLikedPage(5L))
      .thenReturn(List.of(foo, bar));
    Mockito.when(pageService.getPageManagers(6L)).thenReturn(List.of(bar, baz));

    Map<Person, Integer> result = notifiedInteractorService.getInteractorsInterestedInInteraction(
      comment
    );

    assertEquals(
      Map.of(
        foo,
        PersonSettingsService.NOTIFY_PAGE_INTERACTION,
        bar,
        PersonSettingsService.NOTIFY_COMMENT,
        baz,
        PersonSettingsService.NOTIFY_COMMENT
      ),
      result
    );
  }
}
