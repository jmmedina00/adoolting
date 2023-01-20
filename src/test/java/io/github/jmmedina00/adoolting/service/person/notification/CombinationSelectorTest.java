package io.github.jmmedina00.adoolting.service.person.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
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
public class CombinationSelectorTest {
  @MockBean
  private CommentSelector commentSelector;

  @MockBean
  private PageFollowersSelector followerSelector;

  @MockBean
  private IncludeReceiverSelector receiverSelector;

  @Autowired
  private CombinationSelector selector;

  @Test
  public void getPersonNotificationMapIsTheCombinationOfAllOtherSelectorsCombined() {
    Person fromComment1 = new Person();
    Person fromComment2 = new Person();
    Person follower1 = new Person();
    Person follower2 = new Person();
    Person follower3 = new Person();
    Person receiver = new Person();

    Post post = new Post();

    Mockito
      .when(commentSelector.getPersonNotificationMap(post))
      .thenReturn(Map.of(fromComment1, 1, fromComment2, 1));
    Mockito
      .when(followerSelector.getPersonNotificationMap(post))
      .thenReturn(Map.of(follower1, 2, follower2, 2, follower3, 2));
    Mockito
      .when(receiverSelector.getPersonNotificationMap(post))
      .thenReturn(Map.of(receiver, 3));

    Map<Person, Integer> expected = Map.of(
      fromComment1,
      1,
      fromComment2,
      1,
      follower1,
      2,
      follower2,
      2,
      follower3,
      2,
      receiver,
      3
    );
    Map<Person, Integer> result = selector.getPersonNotificationMap(post);
    assertEquals(expected, result);
  }

  @Test
  public void getInteractorsInterestedInInteractionGivesReceiverCodeHigherPriorityThanLikeCode() {
    Person foo = new Person();
    Person bar = new Person();
    Person baz = new Person();

    Post post = new Post();

    Mockito
      .when(followerSelector.getPersonNotificationMap(post))
      .thenReturn(Map.of(foo, 2, bar, 2));
    Mockito
      .when(receiverSelector.getPersonNotificationMap(post))
      .thenReturn(Map.of(bar, 3, baz, 3));

    Map<Person, Integer> result = selector.getPersonNotificationMap(post);

    assertEquals(Map.of(foo, 2, bar, 3, baz, 3), result);
  }

  @Test
  public void getInteractorsInterestedInInteractionGivesCommentCodeHigherPriorityThanLikeCode() {
    Person foo = new Person();
    Person bar = new Person();
    Person baz = new Person();

    Comment comment = new Comment();

    Mockito
      .when(followerSelector.getPersonNotificationMap(comment))
      .thenReturn(Map.of(foo, 2, bar, 2));
    Mockito
      .when(commentSelector.getPersonNotificationMap(comment))
      .thenReturn(Map.of(bar, 1, baz, 1));

    Map<Person, Integer> result = selector.getPersonNotificationMap(comment);

    assertEquals(Map.of(foo, 2, bar, 1, baz, 1), result);
  }
}
