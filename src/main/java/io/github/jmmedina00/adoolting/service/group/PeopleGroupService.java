package io.github.jmmedina00.adoolting.service.group;

import io.github.jmmedina00.adoolting.dto.group.NewEvent;
import io.github.jmmedina00.adoolting.dto.group.NewGroup;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.Event;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.group.PeopleGroupRepository;
import io.github.jmmedina00.adoolting.service.page.PageManagerService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeopleGroupService {
  @Autowired
  private PeopleGroupRepository groupRepository;

  @Autowired
  private PageManagerService pageManagerService;

  @Autowired
  private PersonService personService;

  public PeopleGroup getGroup(Long groupId) {
    return groupRepository.findById(groupId).orElseThrow();
  }

  public PeopleGroup createGroup(NewGroup newGroup, Long personId) {
    Person person = personService.getPerson(personId);
    PeopleGroup group = new PeopleGroup();
    group.setName(newGroup.getName());
    group.setDescription(newGroup.getDescription());
    group.setInteractor(person);
    return groupRepository.save(group);
  }

  public PeopleGroup updateGroup(
    Long groupId,
    Long personId,
    NewGroup newGroup
  )
    throws NotAuthorizedException {
    if (!isGroupManagedByPerson(groupId, personId)) {
      throw new NotAuthorizedException();
    }

    PeopleGroup group = getGroup(groupId);
    group.setName(newGroup.getName());
    group.setDescription(newGroup.getDescription());
    return groupRepository.save(group);
  }

  public NewGroup getGroupForm(Long groupId) {
    PeopleGroup group = getGroup(groupId);
    NewGroup form = new NewGroup();

    if (group instanceof Event) {
      NewEvent eventForm = new NewEvent();
      eventForm.setLocation(((Event) group).getLocation());
      // Datetime: only when view supports it
      form = eventForm;
    }

    form.setDescription(group.getDescription());
    form.setName(group.getName());

    return form;
  }

  public boolean isGroupManagedByPerson(Long groupId, Long personId) {
    PeopleGroup group;

    try {
      group = getGroup(groupId);
    } catch (Exception e) {
      return false;
    }

    Interactor interactor = group.getInteractor();

    if (interactor instanceof Person) {
      return Objects.equals(interactor.getId(), personId);
    }

    Person matchingManager = pageManagerService
      .getPeopleManagingPage(interactor.getId())
      .stream()
      .filter(person -> Objects.equals(person.getId(), personId))
      .findFirst()
      .orElse(null);
    return matchingManager != null;
  }
}
