package io.github.jmmedina00.adoolting.service.interaction;

import io.github.jmmedina00.adoolting.dto.interaction.NewComment;
import io.github.jmmedina00.adoolting.dto.interaction.NewPost;
import io.github.jmmedina00.adoolting.dto.interaction.ProfilePictureFile;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.interaction.ProfilePicture;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.interaction.ProfilePictureRepository;
import io.github.jmmedina00.adoolting.service.MediumService;
import io.github.jmmedina00.adoolting.service.group.PeopleGroupService;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.io.FilenameUtils;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfilePictureService {
  @Autowired
  private ProfilePictureRepository pfpRepository;

  @Autowired
  private JobScheduler jobScheduler;

  @Autowired
  private MediumService mediumService;

  @Autowired
  private PostService postService;

  @Autowired
  private CommentService commentService;

  @Autowired
  private PageService pageService;

  @Autowired
  private PeopleGroupService groupService;

  // TODO: common parts to their own methods

  public ProfilePicture getProfilePictureOfInteractor(Long interactorId) {
    return pfpRepository
      .findInteractorsProfilePictures(interactorId)
      .stream()
      .findFirst()
      .get();
  }

  public ProfilePicture getProfilePictureOfGroup(Long groupId) {
    return pfpRepository
      .findGroupsProfilePictures(groupId)
      .stream()
      .findFirst()
      .get();
  }

  public ProfilePicture setProfilePictureOfInteractor(
    Long interactorId,
    Long attemptingPersonId,
    ProfilePictureFile pfpFile
  )
    throws NotAuthorizedException {
    if (
      !isPersonAuthorizedToChangeInteractor(interactorId, attemptingPersonId)
    ) {
      throw new NotAuthorizedException();
    }

    MultipartFile file = pfpFile.getFile();
    String extension = FilenameUtils.getExtension(file.getOriginalFilename());

    NewPost postTemplate = new NewPost();
    postTemplate.setContents("");
    postTemplate.setMedia(List.of());
    Post post = postService.createPost(interactorId, postTemplate);

    ProfilePicture profilePicture = new ProfilePicture();
    profilePicture.setInteraction(post);
    profilePicture.setReference("cdn:" + "." + extension);

    ProfilePicture saved = pfpRepository.save(profilePicture);
    writeFile(saved, file, extension);
    return saved;
  }

  public ProfilePicture setProfilePictureOfGroup(
    Long groupId,
    Long attemptingPersonId,
    ProfilePictureFile pfpFile
  )
    throws NotAuthorizedException {
    if (!groupService.isGroupManagedByPerson(groupId, attemptingPersonId)) {
      throw new NotAuthorizedException();
    }

    PeopleGroup group = groupService.getGroup(groupId);

    MultipartFile file = pfpFile.getFile();
    String extension = FilenameUtils.getExtension(file.getOriginalFilename());

    NewComment commentTemplate = new NewComment();
    commentTemplate.setContent("");
    Comment comment = commentService.createComment(
      commentTemplate,
      group.getInteractor().getId(),
      groupId
    );

    ProfilePicture profilePicture = new ProfilePicture();
    profilePicture.setInteraction(comment);
    profilePicture.setReference("cdn:" + "." + extension);

    ProfilePicture saved = pfpRepository.save(profilePicture);
    writeFile(saved, file, extension);
    return saved;
  }

  private void writeFile(
    ProfilePicture saved,
    MultipartFile file,
    String extension
  ) {
    String path =
      mediumService.getProperFullPath() + "/" + saved.getId() + "." + extension;

    File writingFile = new File(path);
    try {
      file.transferTo(writingFile);
      jobScheduler.enqueue(() -> mediumService.getImageSquare(saved.getId()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean isPersonAuthorizedToChangeInteractor(
    Long interactorId,
    Long personId
  ) {
    if (Objects.equals(interactorId, personId)) {
      return true;
    }

    Optional<Person> matchingPerson = pageService
      .getPageManagers(interactorId)
      .stream()
      .filter(person -> Objects.equals(person.getId(), personId))
      .findFirst();
    return matchingPerson.isPresent();
  }
}
