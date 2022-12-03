package io.github.jmmedina00.adoolting.service.interaction;

import io.github.jmmedina00.adoolting.dto.interaction.NewComment;
import io.github.jmmedina00.adoolting.dto.interaction.NewPost;
import io.github.jmmedina00.adoolting.dto.interaction.ProfilePictureFile;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.interaction.ProfilePicture;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.interaction.ProfilePictureRepository;
import io.github.jmmedina00.adoolting.service.MediumService;
import java.io.File;
import java.util.List;
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

  public String getProfilePictureOfInteractor(Long interactorId) {
    List<ProfilePicture> pfps = pfpRepository.findInteractorsProfilePictures(
      interactorId
    );
    ProfilePicture latest = pfps.stream().findFirst().get();
    return (
      mediumService.getProperPublicPath() +
      latest.getId() +
      latest.getReference().replace("cdn:", "")
    );
  }

  public String getProfilePictureOfGroup(Long groupId) {
    List<ProfilePicture> pfps = pfpRepository.findGroupsProfilePictures(
      groupId
    );
    ProfilePicture latest = pfps.stream().findFirst().get();
    return (
      mediumService.getProperPublicPath() +
      latest.getId() +
      latest.getReference().replace("cdn:", "")
    );
  }

  public ProfilePicture setProfilePictureOfInteractor(
    Interactor interactor,
    ProfilePictureFile pfpFile
  ) {
    MultipartFile file = pfpFile.getFile();
    String extension = FilenameUtils.getExtension(file.getOriginalFilename());

    NewPost postTemplate = new NewPost();
    postTemplate.setContents("");
    postTemplate.setMedia(List.of());
    Post post = postService.createPost(interactor, postTemplate);

    ProfilePicture profilePicture = new ProfilePicture();
    profilePicture.setInteraction(post);
    profilePicture.setReference("cdn:" + "." + extension);

    ProfilePicture saved = pfpRepository.save(profilePicture);
    writeFile(saved, file, extension);
    return saved;
  }

  public ProfilePicture setProfilePictureOfGroup(
    PeopleGroup group,
    Person owner,
    ProfilePictureFile pfpFile
  ) {
    MultipartFile file = pfpFile.getFile();
    String extension = FilenameUtils.getExtension(file.getOriginalFilename());

    NewComment commentTemplate = new NewComment();
    commentTemplate.setContent("");
    Comment comment = commentService.createComment(
      commentTemplate,
      owner,
      group
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
}
