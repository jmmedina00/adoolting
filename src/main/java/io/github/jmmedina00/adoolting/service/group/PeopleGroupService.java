package io.github.jmmedina00.adoolting.service.group;

import io.github.jmmedina00.adoolting.dto.group.NewEvent;
import io.github.jmmedina00.adoolting.dto.group.NewGroup;
import io.github.jmmedina00.adoolting.dto.util.SecureDeletion;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.Event;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.group.PeopleGroupRepository;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeopleGroupService {
  @Autowired
  private PeopleGroupRepository groupRepository;

  @Autowired
  private PageService pageService;

  @Autowired
  private PersonService personService;

  public PeopleGroup getGroup(Long groupId) {
    return groupRepository.findActiveGroup(groupId).orElseThrow();
  }

  public PeopleGroup createGroup(NewGroup newGroup, Long personId) {
    Person person = personService.getPerson(personId);
    PeopleGroup group = new PeopleGroup();
    group.setName(newGroup.getName());
    group.setDescription(newGroup.getDescription());
    group.setInteractor(person);
    group.setAccessLevel(newGroup.getAccessLevel());
    return groupRepository.save(group);
  }

  public List<PeopleGroup> getGroupsManagedByPerson(Long personId) {
    ArrayList<Interactor> interactors = new ArrayList<>(
      pageService.getAllPersonPages(personId)
    );
    interactors.add(personService.getPerson(personId));
    List<Long> ids = interactors
      .stream()
      .map(interactor -> interactor.getId())
      .sorted()
      .toList();
    return groupRepository.findActiveGroupsByInteractorList(ids);
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
    group.setAccessLevel(newGroup.getAccessLevel());
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

    form.setAccessLevel(group.getAccessLevel());
    form.setDescription(group.getDescription());
    form.setName(group.getName());

    return form;
  }

  public PeopleGroup deleteGroup(
    Long groupId,
    Long attemptingPersonId,
    SecureDeletion confirmation
  )
    throws Exception {
    Person person = personService.getPersonWithMatchingPassword(
      attemptingPersonId,
      confirmation
    );

    if (!isGroupManagedByPerson(groupId, person.getId())) {
      throw new NotAuthorizedException();
    }

    PeopleGroup group = getGroup(groupId);
    group.setDeletedAt(new Date());
    return groupRepository.save(group);
  }

  public boolean isGroupManagedByPerson(Long groupId, Long personId) {
    try {
      PeopleGroup group = getGroup(groupId);
      Interactor interactor = group.getInteractor();

      if (interactor instanceof Person) {
        return Objects.equals(interactor.getId(), personId);
      }

      return pageService.isPageManagedByPerson(interactor.getId(), personId);
    } catch (Exception e) {
      return false;
    }
  }
}
