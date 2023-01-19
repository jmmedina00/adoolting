package io.github.jmmedina00.adoolting.entity.person;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.person.notification.CommentStrategy;
import io.github.jmmedina00.adoolting.entity.person.notification.ConfirmableStrategy;
import io.github.jmmedina00.adoolting.entity.person.notification.DataStrategy;
import io.github.jmmedina00.adoolting.entity.person.notification.InteractionStrategy;
import io.github.jmmedina00.adoolting.entity.person.notification.JoinRequestStrategy;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class NotificationTest {
  MockedConstruction<InteractionStrategy> interactionMock;
  MockedConstruction<CommentStrategy> commentMock;
  MockedConstruction<ConfirmableStrategy> confirmableMock;
  MockedConstruction<JoinRequestStrategy> joinRequestMock;

  List<MockedConstruction<? extends DataStrategy>> mocks;

  @BeforeEach
  public void initializeConstructorMocks() {
    interactionMock = mockConstruction(InteractionStrategy.class);
    commentMock = mockConstruction(CommentStrategy.class);
    confirmableMock = mockConstruction(ConfirmableStrategy.class);
    joinRequestMock = mockConstruction(JoinRequestStrategy.class);

    mocks =
      List.of(interactionMock, commentMock, confirmableMock, joinRequestMock);
  }

  @AfterEach
  public void closeConstructorMocks() {
    interactionMock.closeOnDemand();
    commentMock.closeOnDemand();
    confirmableMock.closeOnDemand();
    joinRequestMock.closeOnDemand();
  }

  @Test
  public void getEmailDataGoesWithInteractionStrategyByDefault() {
    Post post = new Post();
    Person person = new Person();

    Notification notification = new Notification();
    notification.setForPerson(person);
    notification.setInteraction(post);

    notification.getEmailData();

    assertWantedMethodWasCalled(interactionMock, post, person);
  }

  @Test
  public void getEmailDataChoosesCommentStrategyIfInteractionIsAComment() {
    Comment interaction = new Comment();
    Person person = new Person();

    Notification notification = new Notification();
    notification.setForPerson(person);
    notification.setInteraction(interaction);

    notification.getEmailData();

    assertWantedMethodWasCalled(commentMock, interaction, person);
  }

  @Test
  public void getEmailDataChoosesConfirmableStrategyIfInteractionIsConfirmable() {
    ConfirmableInteraction interaction = new ConfirmableInteraction();
    Person person = new Person();

    Notification notification = new Notification();
    notification.setForPerson(person);
    notification.setInteraction(interaction);

    notification.getEmailData();

    assertWantedMethodWasCalled(confirmableMock, interaction, person);
  }

  @Test
  public void getEmailDataChoosesJoinRequestStrategyIfInteractionIsAJoinRequest() {
    JoinRequest interaction = new JoinRequest();
    Person person = new Person();

    Notification notification = new Notification();
    notification.setForPerson(person);
    notification.setInteraction(interaction);

    notification.getEmailData();

    assertWantedMethodWasCalled(joinRequestMock, interaction, person);
  }

  private void assertWantedMethodWasCalled(
    MockedConstruction<? extends DataStrategy> strategyMock,
    Interaction interaction,
    Person person
  ) {
    int index = mocks.indexOf(strategyMock);

    IntStream
      .range(0, mocks.size())
      .filter(i -> i != index)
      .mapToObj(i -> mocks.get(i))
      .forEach(mock -> assertItWasNotCalled(mock, interaction, person));

    assertItWasCalled(strategyMock, interaction, person);
  }

  private void assertItWasCalled(
    MockedConstruction<? extends DataStrategy> strategyMock,
    Interaction interaction,
    Person person
  ) {
    if (strategyMock.constructed().isEmpty()) {
      fail("Mock was not used");
    }

    verify(strategyMock.constructed().get(0), times(1))
      .generateData(interaction, person);
  }

  private void assertItWasNotCalled(
    MockedConstruction<? extends DataStrategy> strategyMock,
    Interaction interaction,
    Person person
  ) {
    if (strategyMock.constructed().isEmpty()) {
      return;
    }

    verify(strategyMock.constructed().get(0), never())
      .generateData(interaction, person);
  }
}
