package io.github.jmmedina00.adoolting.service.group;

import io.github.jmmedina00.adoolting.dto.group.NewGroup;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.group.PeopleGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeopleGroupService {
  @Autowired
  private PeopleGroupRepository groupRepository;

  public PeopleGroup createGroup(NewGroup newGroup, Person person) {
    PeopleGroup group = new PeopleGroup();
    group.setName(newGroup.getName());
    group.setDescription(newGroup.getDescription());
    group.setInteractor(person);
    return groupRepository.save(group);
  }
}
