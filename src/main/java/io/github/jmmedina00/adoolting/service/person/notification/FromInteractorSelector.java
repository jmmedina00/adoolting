package io.github.jmmedina00.adoolting.service.person.notification;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public abstract class FromInteractorSelector implements PersonSelector {
  private PageService pageService;

  public FromInteractorSelector(PageService pageService) {
    this.pageService = pageService;
  }

  public abstract List<Interactor> getInterestedPeopleInInteraction(
    Interaction interaction
  );

  public abstract int getNotificationCode();

  public Map<Person, Integer> getPersonNotificationMap(
    Interaction interaction
  ) {
    return getInterestedPeopleInInteraction(interaction)
      .stream()
      .flatMap(
        interactor -> getPersonsRepresentingInteractor(interactor).stream()
      )
      .map(person -> Map.entry(person, getNotificationCode()))
      .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private List<Person> getPersonsRepresentingInteractor(Interactor interactor) {
    if (interactor instanceof Person) {
      return List.of((Person) interactor);
    }

    return pageService.getPageManagers(interactor.getId());
  }
}
