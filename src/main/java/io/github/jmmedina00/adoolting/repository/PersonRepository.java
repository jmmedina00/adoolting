package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.person.Person;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PersonRepository extends JpaRepository<Person, Long> {
  Person findByEmail(String email);

  @Query(
    "SELECT p FROM Person p WHERE p.id IN (SELECT c.person.id FROM ConfirmationToken c WHERE c.confirmedAt IS NOT NULL)"
  )
  List<Person> findConfirmedPersons();
}
