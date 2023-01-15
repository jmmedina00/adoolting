package io.github.jmmedina00.adoolting.service.interaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.dto.interaction.NewComment;
import io.github.jmmedina00.adoolting.entity.enums.AccessLevel;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.interaction.CommentRepository;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.MediumService;
import io.github.jmmedina00.adoolting.service.person.PersonAccessLevelService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class CommentServiceTest {
  @MockBean
  private CommentRepository commentRepository;

  @MockBean
  private MediumService mediumService;

  @MockBean
  private InteractorService interactorService;

  @MockBean
  private InteractionService interactionService;

  @MockBean
  private PersonAccessLevelService accessLevelService;

  @Autowired
  private CommentService commentService;

  @Test
  public void createCommentCreatesCommentWithPayloadData() throws Exception {
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Person commenter = new Person();
    Person originalCreator = new Person();
    Post post = new Post();
    post.setInteractor(originalCreator);

    Mockito
      .when(interactorService.getRepresentableInteractorByPerson(2L, 2L))
      .thenReturn(commenter);
    Mockito.when(interactionService.getInteraction(3L)).thenReturn(post);
    Mockito
      .when(accessLevelService.getAccessLevelThatPersonHasOnInteraction(2L, 3L))
      .thenReturn(AccessLevel.OPEN);

    NewComment newComment = new NewComment();
    MockMultipartFile file = new MockMultipartFile("test", "test".getBytes());
    newComment.setPostAs(2L);
    newComment.setContent("Test");
    newComment.setFile(file);

    Comment comment = commentService.createComment(newComment, 2L, 3L);
    assertEquals(newComment.getContent(), comment.getContent());
    assertEquals(commenter, comment.getInteractor());
    assertEquals(post, comment.getReceiverInteraction());

    verify(mediumService, times(1)).saveAllFiles(List.of(file), comment);
  }

  @Test
  public void createCommentCreatesCommentAsPageInInteractionCreatedByAnotherPage()
    throws Exception {
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Page commenter = new Page();
    Page originalCreator = new Page();
    Post post = new Post();
    post.setInteractor(originalCreator);

    Mockito
      .when(accessLevelService.getAccessLevelThatPersonHasOnInteraction(2L, 3L))
      .thenReturn(AccessLevel.OPEN);
    Mockito
      .when(interactorService.getRepresentableInteractorByPerson(5L, 2L))
      .thenReturn(commenter);
    Mockito.when(interactionService.getInteraction(3L)).thenReturn(post);

    NewComment newComment = new NewComment();
    MockMultipartFile file = new MockMultipartFile("test", "test".getBytes());
    newComment.setPostAs(5L);
    newComment.setContent("Test");
    newComment.setFile(file);

    Comment comment = commentService.createComment(newComment, 2L, 3L);
    assertEquals(newComment.getContent(), comment.getContent());
    assertEquals(commenter, comment.getInteractor());
    assertEquals(post, comment.getReceiverInteraction());
  }

  @Test
  public void createCommentCreatesCommentWhenInteractionCreatedByPersonButReceivedByPage()
    throws Exception {
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Page page = new Page();
    Person originalCreator = new Person();
    Post post = new Post();
    post.setInteractor(originalCreator);
    post.setReceiverInteractor(page);

    Mockito
      .when(accessLevelService.getAccessLevelThatPersonHasOnInteraction(2L, 3L))
      .thenReturn(AccessLevel.OPEN);
    Mockito
      .when(interactorService.getRepresentableInteractorByPerson(5L, 2L))
      .thenReturn(page);
    Mockito
      .when(interactionService.isInteractionDeletableByPerson(3L, 2L))
      .thenReturn(true);
    Mockito.when(interactionService.getInteraction(3L)).thenReturn(post);

    NewComment newComment = new NewComment();
    MockMultipartFile file = new MockMultipartFile("test", "test".getBytes());
    newComment.setPostAs(5L);
    newComment.setContent("Test");
    newComment.setFile(file);

    Comment comment = commentService.createComment(newComment, 2L, 3L);
    assertEquals(newComment.getContent(), comment.getContent());
    assertEquals(page, comment.getInteractor());
    assertEquals(post, comment.getReceiverInteraction());
  }

  @Test
  public void createCommentThrowsWhenPageTriesToCommentOnUndeletableAndUninvolvedInteractionCreatedByPerson()
    throws Exception {
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Page commenter = new Page();
    Page page = new Page();
    Person originalCreator = new Person();
    Post post = new Post();
    post.setInteractor(originalCreator);
    post.setReceiverInteractor(page);

    Mockito
      .when(interactorService.getRepresentableInteractorByPerson(5L, 2L))
      .thenReturn(commenter);
    Mockito
      .when(interactionService.isInteractionDeletableByPerson(3L, 2L))
      .thenReturn(false);
    Mockito.when(interactionService.getInteraction(3L)).thenReturn(post);

    NewComment newComment = new NewComment();
    MockMultipartFile file = new MockMultipartFile("test", "test".getBytes());
    newComment.setPostAs(5L);
    newComment.setContent("Test");
    newComment.setFile(file);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        commentService.createComment(newComment, 2L, 3L);
      }
    );
  }

  @Test
  public void createCommentThrowsWhenPersonTriesToCommentOnNonOpenInteraction()
    throws NotAuthorizedException {
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Person creator = new Person();
    creator.setId(7L);
    Page receiver = new Page();
    receiver.setId(8L);

    Post post = new Post();
    post.setInteractor(creator);
    post.setReceiverInteractor(receiver);

    Person commenter = new Person();
    commenter.setId(4L);

    NewComment newComment = new NewComment();
    MockMultipartFile file = new MockMultipartFile("test", "test".getBytes());
    newComment.setPostAs(4L);
    newComment.setContent("Test");
    newComment.setFile(file);

    Mockito
      .when(
        accessLevelService.getAccessLevelThatPersonHasOnInteraction(4L, 12L)
      )
      .thenReturn(AccessLevel.WATCH_ONLY);
    Mockito
      .when(interactorService.getRepresentableInteractorByPerson(4L, 4L))
      .thenReturn(commenter);
    Mockito.when(interactionService.getInteraction(12L)).thenReturn(post);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        commentService.createComment(newComment, 4L, 12L);
      }
    );
  }

  @Test
  public void createCommentNotDisruptedByMediumServiceException()
    throws Exception {
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Person commenter = new Person();
    Person originalCreator = new Person();
    Post post = new Post();
    post.setInteractor(originalCreator);

    Mockito
      .when(accessLevelService.getAccessLevelThatPersonHasOnInteraction(2L, 3L))
      .thenReturn(AccessLevel.OPEN);
    Mockito
      .when(interactorService.getRepresentableInteractorByPerson(2L, 2L))
      .thenReturn(commenter);
    Mockito.when(interactionService.getInteraction(3L)).thenReturn(post);

    Mockito
      .doThrow(Exception.class)
      .when(mediumService)
      .saveAllFiles(any(), any());

    NewComment newComment = new NewComment();
    MockMultipartFile file = new MockMultipartFile("test", "test".getBytes());
    newComment.setPostAs(2L);
    newComment.setContent("Test");
    newComment.setFile(file);

    assertDoesNotThrow(
      () -> {
        commentService.createComment(newComment, 2L, 3L);
      }
    );
  }
}
