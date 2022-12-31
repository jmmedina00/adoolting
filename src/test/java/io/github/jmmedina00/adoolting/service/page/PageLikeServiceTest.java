package io.github.jmmedina00.adoolting.service.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.page.PageLike;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.page.PageLikeRepository;
import io.github.jmmedina00.adoolting.service.InteractorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PageLikeServiceTest {
  @MockBean
  private PageLikeRepository likeRepository;

  @MockBean
  private InteractorService interactorService;

  @Autowired
  private PageLikeService likeService;

  @BeforeEach
  public void setUpMocks() {
    Mockito
      .when(likeRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  public void toggleLikeToPageCreatesLikeIfNotExisting() {
    Page page = new Page();
    Person person = new Person();

    Mockito.when(interactorService.getInteractor(1L)).thenReturn(person);
    Mockito.when(interactorService.getInteractor(2L)).thenReturn(page);

    PageLike like = likeService.toggleLikeToPage(1L, 2L);
    assertEquals(person, like.getInteractor());
    assertEquals(page, like.getReceiverInteractor());

    verify(likeRepository, times(1)).save(like);
  }

  @Test
  public void toggleLikeToPageSetsExistingLikeToDeleted() {
    PageLike like = new PageLike();
    Mockito
      .when(likeRepository.findPageLikeFromPerson(1L, 2L))
      .thenReturn(like);

    PageLike result = likeService.toggleLikeToPage(1L, 2L);
    assertNotNull(result.getDeletedAt());

    verify(likeRepository, times(1)).save(like);
  }

  @Test
  public void toggleLikeToPageOnlyProcessesPersonIdAsFirstArgument() {
    Page page = new Page();
    Mockito.when(interactorService.getInteractor(1L)).thenReturn(page);
    Mockito.when(interactorService.getInteractor(2L)).thenReturn(page);

    assertThrows(
      ClassCastException.class,
      () -> {
        likeService.toggleLikeToPage(1L, 2L);
      }
    );
  }

  @Test
  public void toggleLikeToPageOnlyProcessesPageIdAsSecondArgument() {
    Person person = new Person();
    Mockito.when(interactorService.getInteractor(1L)).thenReturn(person);
    Mockito.when(interactorService.getInteractor(2L)).thenReturn(person);

    assertThrows(
      ClassCastException.class,
      () -> {
        likeService.toggleLikeToPage(1L, 2L);
      }
    );
  }
}
