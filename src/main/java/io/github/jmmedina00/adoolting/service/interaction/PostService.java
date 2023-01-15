package io.github.jmmedina00.adoolting.service.interaction;

import io.github.jmmedina00.adoolting.dto.interaction.NewPost;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.enums.AccessLevel;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.MediumService;
import io.github.jmmedina00.adoolting.service.person.PersonAccessLevelService;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {
  @Autowired
  private MediumService mediumService;

  @Autowired
  private InteractorService interactorService;

  @Autowired
  private InteractionService interactionService;

  @Autowired
  private PersonAccessLevelService accessLevelService;

  private static final Logger logger = LoggerFactory.getLogger(
    PostService.class
  );

  public Post createPost(Long interactorId, NewPost newPost) {
    Interactor interactor = interactorService.getInteractor(interactorId);

    Post post = new Post();
    post.setInteractor(interactor);
    post.setContent(newPost.getContent().trim());
    Post saved = (Post) interactionService.saveInteraction(post);

    logger.info(
      "New post (id={}) created by interactor {}",
      saved.getId(),
      interactorId
    );

    handleNewPostFiles(newPost, saved);
    return saved;
  }

  public Post postOnProfile(
    Long personId,
    Long receiverInteractorId,
    NewPost newPost
  )
    throws NotAuthorizedException {
    Long interactorId = newPost.getPostAs();
    Interactor interactor = interactorService.getRepresentableInteractorByPerson(
      interactorId,
      personId
    );

    if (Objects.equals(interactorId, receiverInteractorId)) {
      return createPost(interactorId, newPost);
    }

    if (
      accessLevelService.getAccessLevelThatPersonHasOnInteractor(
        personId,
        receiverInteractorId
      ) !=
      AccessLevel.OPEN
    ) {
      throw new NotAuthorizedException();
    }

    Interactor receiverInteractor = interactorService.getInteractor(
      receiverInteractorId
    );

    if (interactor instanceof Page && receiverInteractor instanceof Person) {
      throw new NotAuthorizedException();
    }

    Post post = new Post();
    post.setInteractor(interactor);
    post.setReceiverInteractor(receiverInteractor);
    post.setContent(newPost.getContent().trim());
    Post saved = (Post) interactionService.saveInteraction(post);

    logger.info(
      "New post (id={}) created by interactor {} on interactor {}'s profile.",
      saved.getId(),
      interactorId,
      receiverInteractorId
    );

    handleNewPostFiles(newPost, saved);
    return saved;
  }

  private void handleNewPostFiles(NewPost post, Post savedPost) {
    if (!Optional.ofNullable(post.getUrl()).orElse("").isEmpty()) {
      logger.debug("Saving saved post {} link to cache", savedPost.getId());
      mediumService.saveLinkMedium(post.getUrl(), savedPost);
      return;
    }

    try {
      logger.debug("Saving saved post {} files to CDN.", savedPost.getId());
      mediumService.saveAllFiles(post.getMedia(), savedPost);
    } catch (Exception e) {
      logger.error(
        "An exception occurred while saving files from post {}",
        savedPost.getId(),
        e
      );
    }
  }
}
