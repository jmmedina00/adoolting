package io.github.jmmedina00.adoolting.entity.person.notification;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import java.util.List;

public class CommentStrategy extends InteractionStrategy {

  @Override
  public List<Interactor> getInterestingInteractors(Interaction interaction) {
    Comment comment = (Comment) interaction;
    Interaction commented = comment.getReceiverInteraction();

    Interactor commenter = comment.getInteractor();
    Interactor originalInteractor = commented.getInteractor();
    Interactor originalReceiver = commented.getReceiverInteractor();

    return originalReceiver == null
      ? List.of(commenter, originalInteractor)
      : List.of(commenter, originalInteractor, originalReceiver);
  }
}
