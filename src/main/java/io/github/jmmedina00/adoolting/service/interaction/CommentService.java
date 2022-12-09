package io.github.jmmedina00.adoolting.service.interaction;

import io.github.jmmedina00.adoolting.dto.interaction.NewComment;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.repository.interaction.CommentRepository;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.MediumService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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

  public List<Comment> getCommentsFromInteraction(Long interactionId) {
    return commentRepository.findByReceiverInteractionId(interactionId);
  }

  public Comment createComment(
    NewComment newComment,
    Long interactorId,
    Long interactionId
  ) {
    Interactor interactor = interactorService.getInteractor(interactorId);
    Interaction interaction = interactionService.getInteraction(interactionId);

    Comment comment = new Comment();
    comment.setContent(newComment.getContent());
    comment.setInteractor(interactor);
    comment.setReceiverInteraction(interaction);

    Comment saved = commentRepository.save(comment);
    if (newComment.getFile() == null) {
      return saved;
    }

    try {
      mediumService.saveAllFiles(List.of(newComment.getFile()), comment);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return saved;
  }
}
