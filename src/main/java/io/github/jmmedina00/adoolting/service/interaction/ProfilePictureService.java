package io.github.jmmedina00.adoolting.service.interaction;

import io.github.jmmedina00.adoolting.dto.interaction.ProfilePictureFile;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.interaction.ProfilePicture;
import io.github.jmmedina00.adoolting.exception.MediumNotFoundException;
import io.github.jmmedina00.adoolting.repository.interaction.ProfilePictureRepository;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.MediumService;
import io.github.jmmedina00.adoolting.service.group.PeopleGroupService;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfilePictureService {
  @Autowired
  private ProfilePictureRepository pfpRepository;

  @Autowired
  private MediumService mediumService;

  @Autowired
  private InteractorService interactorService;

  @Autowired
  private InteractionService interactionService;

  @Autowired
  private PeopleGroupService groupService;

  private static final Logger logger = LoggerFactory.getLogger(
    ProfilePictureService.class
  );

  public ProfilePicture getProfilePictureOfInteractor(Long interactorId)
    throws MediumNotFoundException {
    return pfpRepository
      .findInteractorsProfilePictures(interactorId)
      .stream()
      .findFirst()
      .orElseThrow(MediumNotFoundException::new);
  }

  public ProfilePicture getProfilePictureOfGroup(Long groupId)
    throws MediumNotFoundException {
    return pfpRepository
      .findGroupsProfilePictures(groupId)
      .stream()
      .findFirst()
      .orElseThrow(MediumNotFoundException::new);
  }

  public ProfilePicture setProfilePictureOfInteractor(
    Long interactorId,
    Long attemptingPersonId,
    ProfilePictureFile pfpFile
  )
    throws Exception {
    MultipartFile file = pfpFile.getFile();

    Post post = new Post();
    post.setContent("");
    post.setInteractor(
      interactorService.getRepresentableInteractorByPerson(
        interactorId,
        attemptingPersonId
      )
    );
    interactionService.saveInteraction(post);

    ProfilePicture profilePicture = new ProfilePicture();
    profilePicture.setInteraction(post);

    mediumService.saveImageMedium(profilePicture, file);

    if (Objects.equals(interactorId, attemptingPersonId)) {
      logger.info(
        "Person {} has updated their profile picture. Post id={}, pfp id={}",
        interactorId,
        post.getId(),
        profilePicture.getId()
      );
    } else {
      logger.info(
        "Person {} has updated interactor {}'s profile picture. Post id={}, pfp id={}",
        attemptingPersonId,
        interactorId,
        post.getId(),
        profilePicture.getId()
      );
    }

    return profilePicture;
  }

  public ProfilePicture setProfilePictureOfGroup(
    Long groupId,
    Long attemptingPersonId,
    ProfilePictureFile pfpFile
  )
    throws Exception {
    PeopleGroup group = groupService.getGroupManagedByPerson(
      groupId,
      attemptingPersonId
    );
    MultipartFile file = pfpFile.getFile();

    Comment comment = new Comment();
    comment.setContent("");
    comment.setInteractor(group.getInteractor());
    comment.setReceiverInteraction(group);
    interactionService.saveInteraction(comment);

    ProfilePicture profilePicture = new ProfilePicture();
    profilePicture.setInteraction(comment);

    mediumService.saveImageMedium(profilePicture, file);

    logger.info(
      "Person {} has updated group {}'s profile picture. Comment id={}, pfp id={}",
      attemptingPersonId,
      groupId,
      comment.getId(),
      profilePicture.getId()
    );
    return profilePicture;
  }
}
