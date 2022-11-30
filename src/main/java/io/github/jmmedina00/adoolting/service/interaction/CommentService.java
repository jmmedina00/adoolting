package io.github.jmmedina00.adoolting.service.interaction;

import io.github.jmmedina00.adoolting.dto.interaction.NewComment;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.repository.interaction.CommentRepository;
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

  public List<Comment> getCommentsFromInteraction(Long interactionId) {
    List<Comment> comments = commentRepository.findByReceiverInteractionId(
      interactionId
    );
    return comments
      .stream()
      .map(
        comment -> {
          if (comment.getMedia().size() == 0) {
            return comment;
          }

          comment.setMedia(
            mediumService.getMediaForInteraction(comment.getId())
          );
          return comment;
        }
      )
      .toList();
  }

  public Comment createComment(
    NewComment newComment,
    Interactor interactor,
    Interaction interaction
  ) {
    Comment comment = new Comment();
    comment.setContent(newComment.getContent());
    comment.setInteractor(interactor);
    comment.setReceiverInteraction(interaction);

    Comment saved = commentRepository.save(comment);
    try {
      mediumService.saveAllFiles(List.of(newComment.getFile()), comment);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return saved;
  }
}
