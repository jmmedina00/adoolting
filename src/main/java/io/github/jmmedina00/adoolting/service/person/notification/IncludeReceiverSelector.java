package io.github.jmmedina00.adoolting.service.person.notification;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.PersonSettingsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IncludeReceiverSelector extends FromInteractorSelector {

  @Autowired
  public IncludeReceiverSelector(PageService pageService) {
    super(pageService);
  }

  @Override
  public List<Interactor> getInterestedPeopleInInteraction(
    Interaction interaction
  ) {
    if (interaction.getReceiverInteractor() == null) return List.of();

    return List.of(interaction.getReceiverInteractor());
  }

  @Override
  public int getNotificationCode() {
    return PersonSettingsService.NOTIFY_POST_FROM_OTHER;
  }
}
