package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.InteractorRepository;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InteractorService {
  @Autowired
  private InteractorRepository interactorRepository;

  @Autowired
  private PageService pageService;

  public Interactor getInteractor(Long interactorId) {
    return interactorRepository.findById(interactorId).orElseThrow();
  }

  public List<Interactor> getRepresentableInteractorsByPerson(
    Long personId,
    Long otherInteractorId
  ) {
    Interactor person = (Person) getInteractor(personId);
    Interactor otherInteractor = getInteractor(otherInteractorId);

    if (otherInteractor instanceof Person) {
      return List.of(person);
    }

    if (pageService.isPageManagedByPerson(otherInteractorId, personId)) {
      return List.of(person, otherInteractor);
    }

    ArrayList<Interactor> interactors = new ArrayList<>(
      pageService.getAllPersonPages(personId)
    );
    interactors.add(0, person);
    return interactors;
  }
}
