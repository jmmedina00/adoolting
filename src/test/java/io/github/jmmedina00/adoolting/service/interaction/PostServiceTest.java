package io.github.jmmedina00.adoolting.service.interaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.dto.interaction.NewPost;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.MediumService;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class PostServiceTest {
  @MockBean
  private MediumService mediumService;

  @MockBean
  private InteractorService interactorService;

  @MockBean
  private InteractionService interactionService;

  @MockBean
  private PageService pageService;

  @Autowired
  private PostService postService;

  private Answer<Interactor> generateInteractor() {
    return new Answer<Interactor>() {

      public Interactor answer(InvocationOnMock invocation) throws Throwable {
        Long id = invocation.getArgument(0);
        Interactor interactor = new Person();
        interactor.setId(id);
        return interactor;
      }
    };
  }

  @BeforeEach
  public void setUpMocks() throws NotAuthorizedException {
    Mockito
      .when(interactionService.saveInteraction(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Mockito
      .when(interactorService.getInteractor(anyLong()))
      .thenAnswer(generateInteractor());
    Mockito
      .when(
        interactorService.getRepresentableInteractorByPerson(
          anyLong(),
          anyLong()
        )
      )
      .thenAnswer(generateInteractor());
  }

  @Test
  public void postOnProfileCreatesPostWithSpecifiedDetails()
    throws NotAuthorizedException {
    String content = "This is content";
    NewPost newPost = new NewPost();
    newPost.setPostAs(1L);
    newPost.setContent(content);

    Post post = postService.postOnProfile(1L, 2L, newPost);
    assertEquals(1L, post.getInteractor().getId());
    assertEquals(2L, post.getReceiverInteractor().getId());
    assertEquals(content, post.getContent());
  }

  @Test
  public void postOnProfileThrowsIfTargetedInteractorIsNotRepresentableByPerson()
    throws NotAuthorizedException {
    String content = "This is content";
    NewPost newPost = new NewPost();
    newPost.setPostAs(10L);
    newPost.setContent(content);

    Mockito
      .when(interactorService.getRepresentableInteractorByPerson(10L, 1L))
      .thenThrow(NotAuthorizedException.class);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        postService.postOnProfile(1L, 2L, newPost);
      }
    );
  }

  @Test
  public void postOnProfileSetsReceiverToNullWhenPayloadSpecifiesTheSameInteractorEverywhere()
    throws NotAuthorizedException {
    String content = "This is content";
    NewPost newPost = new NewPost();
    newPost.setPostAs(1L);
    newPost.setContent(content);

    Post post = postService.postOnProfile(1L, 1L, newPost);
    assertEquals(1L, post.getInteractor().getId());
    assertNull(post.getReceiverInteractor());
    assertEquals(content, post.getContent());
  }

  @Test
  public void postOnProfileCallsLinkHandlingWhenUrlIsPopulated()
    throws Exception {
    String url = "http://test.local";
    NewPost newPost = new NewPost();
    newPost.setPostAs(1L);
    newPost.setContent("This is content");
    newPost.setUrl("http://test.local");

    Post post = postService.postOnProfile(1L, 1L, newPost);
    verify(mediumService, never()).saveAllFiles(null, post);
    verify(mediumService, times(1)).saveLinkMedium(url, post);
  }

  @Test
  public void postOnProfileCallsFileHandlingByDefault() throws Exception {
    List<MultipartFile> media = List.of();
    NewPost newPost = new NewPost();
    newPost.setPostAs(1L);
    newPost.setContent("This is content");
    newPost.setUrl("");
    newPost.setMedia(media);

    Post post = postService.postOnProfile(1L, 1L, newPost);
    verify(mediumService, times(1)).saveAllFiles(media, post);
    verify(mediumService, never()).saveLinkMedium("", post);
  }

  @Test
  public void postOnProfileNotGettingDisruptedByMediumServiceException()
    throws Exception {
    Mockito
      .doThrow(Exception.class)
      .when(mediumService)
      .saveAllFiles(any(), any());

    List<MultipartFile> media = List.of();
    NewPost newPost = new NewPost();
    newPost.setPostAs(1L);
    newPost.setContent("This is content");
    newPost.setUrl("");
    newPost.setMedia(media);

    assertDoesNotThrow(
      () -> {
        postService.postOnProfile(1L, 1L, newPost);
      }
    );
  }
}
