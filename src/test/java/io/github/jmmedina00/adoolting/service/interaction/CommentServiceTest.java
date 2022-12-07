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
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.repository.interaction.CommentRepository;
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

  @Autowired
  private CommentService commentService;
  // TODO: refactor tests to keep id refactor into account

  /* @Test
  public void createCommentCreatesCommentWithPayloadData() throws Exception {
    Mockito
      .when(commentRepository.save(any()))
      .thenAnswer(
        invocation -> {
          return invocation.getArgument(0);
        }
      );

    NewComment newComment = new NewComment();
    MockMultipartFile file = new MockMultipartFile("test", "test".getBytes());
    newComment.setContent("Test");
    newComment.setFile(file);

    Interactor interactor = new Page();
    interactor.setAbout("This is a page");
    Interaction interaction = new Interaction();

    Comment comment = commentService.createComment(
      newComment,
      interactor,
      interaction
    );
    assertEquals(comment.getContent(), newComment.getContent());
    assertEquals(comment.getInteractor(), interactor);
    assertEquals(comment.getReceiverInteraction(), interaction);

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

    Interactor interactor = new Page();
    interactor.setAbout("This is a page");
    Interaction interaction = new Interaction();

    assertDoesNotThrow(
      () -> {
        commentService.createComment(newComment, interactor, interaction);
      }
    );
  } */
}
