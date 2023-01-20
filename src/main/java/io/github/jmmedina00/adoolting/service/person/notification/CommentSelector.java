package io.github.jmmedina00.adoolting.service.person.notification;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.PersonSettingsService;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentSelector extends FromInteractorSelector {

  @Autowired
  public CommentSelector(PageService pageService) {
    super(pageService);
  }

  @Override
  public List<Interactor> getInterestedPeopleInInteraction(
    Interaction interaction
  ) {
    if (!(interaction instanceof Comment)) {
      return List.of();
    }

    Comment comment = (Comment) interaction;
    Interactor author = (Interactor) Hibernate.unproxy(comment.getInteractor());
    Interaction commentedOnInteraction = comment.getReceiverInteraction();
    Interactor originalInteractor = commentedOnInteraction.getInteractor();

    if (Objects.equals(originalInteractor.getId(), author.getId())) {
      return List.of();
    }

    return List.of(originalInteractor);
  }

  @Override
  public int getNotificationCode() {
    return PersonSettingsService.NOTIFY_COMMENT;
  }
}
