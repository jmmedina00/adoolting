package io.github.jmmedina00.adoolting.service.interaction;

import io.github.jmmedina00.adoolting.dto.interaction.NewComment;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.enums.AccessLevel;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.interaction.CommentRepository;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.MediumService;
import io.github.jmmedina00.adoolting.service.person.PersonAccessLevelService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private MediumService mediumService;

  @Autowired
  private InteractorService interactorService;

  @Autowired
  private InteractionService interactionService;

  @Autowired
  private PersonAccessLevelService accessLevelService;

  private static final Logger logger = LoggerFactory.getLogger(
    CommentService.class
  );

  public Page<Comment> getCommentsFromInteraction(
    Long interactionId,
    Pageable pageable
  ) {
    return commentRepository.findByReceiverInteraction(interactionId, pageable);
  }

  public Comment createComment(
    NewComment newComment,
    Long personId,
    Long interactionId
  )
    throws NotAuthorizedException {
    Long interactorId = newComment.getPostAs();
    Interactor interactor = interactorService.getRepresentableInteractorByPerson(
      interactorId,
      personId
    );
    Interaction interaction = interactionService.getInteraction(interactionId);

    Interactor author = interaction.getInteractor();

    if (
      !(interactor instanceof Person) &&
      (author instanceof Person) &&
      !(
        interactionService.isInteractionDeletableByPerson(
          interactionId,
          personId
        )
      )
    ) {
      throw new NotAuthorizedException();
    }

    if (
      accessLevelService.getAccessLevelThatPersonHasOnInteraction(
        personId,
        interactionId
      ) !=
      AccessLevel.OPEN
    ) {
      throw new NotAuthorizedException();
    }

    Comment comment = new Comment();
    comment.setContent(newComment.getContent());
    comment.setInteractor(interactor);
    comment.setReceiverInteraction(interaction);

    Comment saved = (Comment) interactionService.saveInteraction(comment);
    logger.info(
      "New comment (id={}) created by interactor {} on interaction {}.",
      comment.getId(),
      interactorId,
      interactionId
    );

    if (newComment.getFile() == null) {
      logger.debug(
        "No file for comment {}. Skipping medium service call.",
        saved.getId()
      );
      return saved;
    }

    try {
      mediumService.saveAllFiles(List.of(newComment.getFile()), comment);
    } catch (Exception e) {
      logger.error(
        "An exception occurred while saving file from comment {}",
        saved.getId(),
        e
      );
    }
    return saved;
  }
}
