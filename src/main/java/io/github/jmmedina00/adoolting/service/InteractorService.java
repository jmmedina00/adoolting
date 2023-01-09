package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.InteractorRepository;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

  public boolean isInteractorRepresentableByPerson(
    Long interactorId,
    Long personId
  ) {
    try {
      getRepresentableInteractorByPerson(interactorId, personId);
      return true;
    } catch (NotAuthorizedException e) {
      return false;
    }
  }

  public Interactor getRepresentableInteractorByPerson(
    Long interactorId,
    Long personId
  )
    throws NotAuthorizedException {
    Person person;
    Interactor interactor;

    try {
      person = (Person) getInteractor(personId);
      interactor = getInteractor(interactorId);
    } catch (Exception e) {
      throw new NotAuthorizedException();
    }

    if (interactor instanceof Person) {
      if (Objects.equals(person.getId(), interactor.getId())) return interactor;
      throw new NotAuthorizedException();
    }

    if (!pageService.isPageManagedByPerson(interactorId, personId)) {
      throw new NotAuthorizedException();
    }

    return interactor;
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
