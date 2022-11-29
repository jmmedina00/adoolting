package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.PeopleGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeopleGroupRepository
  extends JpaRepository<PeopleGroup, Long> {}
