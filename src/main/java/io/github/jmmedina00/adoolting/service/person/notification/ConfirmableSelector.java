package io.github.jmmedina00.adoolting.service.person.notification;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.List;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfirmableSelector extends FromInteractorSelector {

  @Autowired
  public ConfirmableSelector(PageService pageService) {
    super(pageService);
  }

  @Override
  public List<Interactor> getInterestedPeopleInInteraction(
    Interaction interaction
  ) {
    ConfirmableInteraction cInteraction = (ConfirmableInteraction) interaction;

    if (cInteraction.getIgnoredAt() != null) {
      return List.of();
    }

    Interactor author = (Interactor) Hibernate.unproxy(
      interaction.getInteractor()
    );
    Interactor receiver = (Interactor) Hibernate.unproxy(
      interaction.getReceiverInteractor()
    );

    return cInteraction.getConfirmedAt() == null
      ? List.of(receiver)
      : List.of(author);
  }

  @Override
  public int getNotificationCode() {
    return 0;
  }
}
