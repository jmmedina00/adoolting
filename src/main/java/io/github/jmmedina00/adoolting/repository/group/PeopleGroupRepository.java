package io.github.jmmedina00.adoolting.repository.group;

import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeopleGroupRepository
  extends JpaRepository<PeopleGroup, Long> {}
