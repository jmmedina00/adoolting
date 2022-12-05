package io.github.jmmedina00.adoolting.service.group;

import io.github.jmmedina00.adoolting.dto.group.NewGroup;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.group.PeopleGroupRepository;
import io.github.jmmedina00.adoolting.service.page.PageManagerService;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeopleGroupService {
  @Autowired
  private PeopleGroupRepository groupRepository;

  @Autowired
  private PageManagerService pageManagerService;

  public PeopleGroup getGroup(Long groupId) {
    return groupRepository.findById(groupId).orElseThrow();
  }

  public PeopleGroup createGroup(NewGroup newGroup, Person person) {
    PeopleGroup group = new PeopleGroup();
    group.setName(newGroup.getName());
    group.setDescription(newGroup.getDescription());
    group.setInteractor(person);
    return groupRepository.save(group);
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
