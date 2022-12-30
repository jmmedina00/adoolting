package io.github.jmmedina00.adoolting.service.interaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.dto.interaction.ProfilePictureFile;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.interaction.ProfilePicture;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.MediumNotFoundException;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.interaction.ProfilePictureRepository;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.MediumService;
import io.github.jmmedina00.adoolting.service.group.PeopleGroupService;
import io.github.jmmedina00.adoolting.service.page.PageService;
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
public class ProfilePictureServiceTest {
  @MockBean
  private ProfilePictureRepository pfpRepository;

  @MockBean
  private MediumService mediumService;

  @MockBean
  private InteractorService interactorService;

  @MockBean
  private InteractionService interactionService;

  @MockBean
  private PageService pageService;

  @MockBean
  private PeopleGroupService groupService;

  @Autowired
  private ProfilePictureService pfpService;

  @Test
  public void fetchMethodsAlwaysReturnTheFirstResult()
    throws MediumNotFoundException {
    ProfilePicture foo = new ProfilePicture();
    ProfilePicture bar = new ProfilePicture();

    Mockito
      .when(pfpRepository.findGroupsProfilePictures(anyLong()))
      .thenReturn(List.of(foo, bar));
    Mockito
      .when(pfpRepository.findInteractorsProfilePictures(anyLong()))
      .thenReturn(List.of(foo, bar));

    ProfilePicture fromInteractor = pfpService.getProfilePictureOfInteractor(
      1L
    );
    ProfilePicture fromGroup = pfpService.getProfilePictureOfGroup(1L);

    assertEquals(fromInteractor, foo);
    assertEquals(fromGroup, foo);
  }

  @Test
  public void fetchMethodsThrowWhenNoResultsAreFound() {
    Mockito
      .when(pfpRepository.findGroupsProfilePictures(anyLong()))
      .thenReturn(List.of());
    Mockito
      .when(pfpRepository.findInteractorsProfilePictures(anyLong()))
      .thenReturn(List.of());

    assertThrows(
      MediumNotFoundException.class,
      () -> {
        pfpService.getProfilePictureOfInteractor(1L);
      }
    );
    assertThrows(
      MediumNotFoundException.class,
      () -> {
        pfpService.getProfilePictureOfGroup(1L);
      }
    );
  }

  @Test
  public void setProfilePictureOfInteractorCreatesEmptyPostForPfp()
    throws Exception {
    Person person = new Person();
    Mockito.when(interactorService.getInteractor(anyLong())).thenReturn(person);

    ProfilePictureFile pfpFile = new ProfilePictureFile();
    pfpFile.setFile(new MockMultipartFile("test", new byte[] {  }));

    ProfilePicture pfp = pfpService.setProfilePictureOfInteractor(
      1L,
      1L,
      pfpFile
    );

    Post post = (Post) pfp.getInteraction();
    assertEquals("", post.getContent());
    assertEquals(person, post.getInteractor());

    verify(interactionService, times(1)).saveInteraction(post);
    verify(mediumService, times(1)).saveImageMedium(pfp, pfpFile.getFile());
  }

  @Test
  public void setProfilePictureOfGroupCreatesEmptyCommentForPfp()
    throws Exception {
    PeopleGroup group = new PeopleGroup();
    group.setInteractor(new Person());
    Mockito.when(groupService.getGroup(anyLong())).thenReturn(group);
    Mockito
      .when(groupService.isGroupManagedByPerson(anyLong(), anyLong()))
      .thenReturn(true);

    ProfilePictureFile pfpFile = new ProfilePictureFile();
    pfpFile.setFile(new MockMultipartFile("test", new byte[] {  }));

    ProfilePicture pfp = pfpService.setProfilePictureOfGroup(1L, 1L, pfpFile);

    Comment comment = (Comment) pfp.getInteraction();
    assertEquals("", comment.getContent());
    assertEquals(group, comment.getReceiverInteraction());
    assertEquals(group.getInteractor(), comment.getInteractor());

    verify(interactionService, times(1)).saveInteraction(comment);
    verify(mediumService, times(1)).saveImageMedium(pfp, pfpFile.getFile());
  }

  @Test
  public void setProfilePictureOfGroupThrowsWhenPersonIsUnauthorized() {
    Mockito
      .when(groupService.isGroupManagedByPerson(anyLong(), anyLong()))
      .thenReturn(false); // TODO: do this test for interactor pfp too

    ProfilePictureFile pfpFile = new ProfilePictureFile();

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        pfpService.setProfilePictureOfGroup(1L, 1L, pfpFile);
      }
    );
  }
}
