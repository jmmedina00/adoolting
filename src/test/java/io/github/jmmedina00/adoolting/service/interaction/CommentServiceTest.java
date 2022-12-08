package io.github.jmmedina00.adoolting.service.interaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.dto.interaction.NewComment;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.interaction.CommentRepository;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.MediumService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class CommentServiceTest {
  @MockBean
  private CommentRepository commentRepository;

  @MockBean
  private MediumService mediumService;

  @MockBean
  private InteractorService interactorService;

  @MockBean
  private InteractionService interactionService;

  @Autowired
  private CommentService commentService;

  @Test
  public void createCommentCreatesCommentWithPayloadData() throws Exception {
    Mockito
      .when(commentRepository.save(any()))
      .thenAnswer(
        invocation -> {
          return invocation.getArgument(0);
        }
      );

    Mockito
      .when(interactionService.getInteraction(any()))
      .thenAnswer(
        invocation -> {
          Long id = invocation.getArgument(0);
          Interaction interaction = new Interaction();
          interaction.setId(id);
          return interaction;
        }
      );

    Mockito
      .when(interactorService.getInteractor(any()))
      .thenAnswer(
        invocation -> {
          Long id = invocation.getArgument(0);
          Interactor interactor = new Person();
          interactor.setId(id);
          return interactor;
        }
      );

    NewComment newComment = new NewComment();
    MockMultipartFile file = new MockMultipartFile("test", "test".getBytes());
    newComment.setContent("Test");
    newComment.setFile(file);

    Comment comment = commentService.createComment(newComment, 2L, 3L);
    assertEquals(comment.getContent(), newComment.getContent());
    assertEquals(comment.getInteractor().getId(), 2L);
    assertEquals(comment.getReceiverInteraction().getId(), 3L);

    verify(mediumService, times(1)).saveAllFiles(List.of(file), comment);
  }

  @Test
  public void createCommentNotDisruptedByMediumServiceException()
    throws Exception {
    Mockito
      .doThrow(Exception.class)
      .when(mediumService)
      .saveAllFiles(any(), any());

    NewComment newComment = new NewComment();
    MockMultipartFile file = new MockMultipartFile("test", "test".getBytes());
    newComment.setContent("Test");
    newComment.setFile(file);

    assertDoesNotThrow(
      () -> {
        commentService.createComment(newComment, 2L, 3L);
      }
    );
  }
}
