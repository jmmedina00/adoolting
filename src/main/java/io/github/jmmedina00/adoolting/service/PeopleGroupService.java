package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.dto.NewGroup;
import io.github.jmmedina00.adoolting.entity.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.Person;
import io.github.jmmedina00.adoolting.repository.PeopleGroupRepository;
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
