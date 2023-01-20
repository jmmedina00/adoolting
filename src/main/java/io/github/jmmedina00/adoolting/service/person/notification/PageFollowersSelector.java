package io.github.jmmedina00.adoolting.service.person.notification;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import io.github.jmmedina00.adoolting.service.person.PersonSettingsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PageFollowersSelector extends FromInteractorSelector {
  @Autowired
  private PersonService personService;

  @Autowired
  public PageFollowersSelector(PageService pageService) {
    super(pageService);
  }

  @Override
  public List<Interactor> getInterestedPeopleInInteraction(
    Interaction interaction
  ) {
    if (!(interaction.getInteractor() instanceof Page)) return List.of();

    return personService
      .getPersonsWhoLikedPage(interaction.getInteractor().getId())
      .stream()
      .map(person -> (Interactor) person)
      .toList();
  }

  @Override
  public int getNotificationCode() {
    return PersonSettingsService.NOTIFY_PAGE_INTERACTION;
  }
}
