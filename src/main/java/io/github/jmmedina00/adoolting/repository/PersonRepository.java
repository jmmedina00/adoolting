package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
  Person findByEmail(String email);
}
