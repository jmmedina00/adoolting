package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PersonRepository extends JpaRepository<Person, Long> {
  Person findByEmail(String email);

  @Query(
    "SELECT p FROM Person p WHERE p.id IN " +
    "(SELECT c.person.id FROM ConfirmationToken c WHERE c.confirmedAt IS NOT NULL)"
  )
  List<Person> findConfirmedPersons();

  @Query(
    "SELECT p FROM Person p WHERE p.id=:personId AND p.id IN " +
    "(SELECT c.person.id FROM ConfirmationToken c WHERE c.confirmedAt IS NOT NULL)"
  )
  Optional<Person> findActivePerson(@Param("personId") Long personId);

  @Query(
    "SELECT p FROM Person p WHERE p.id IN (SELECT l.interactor.id FROM PageLike l WHERE " +
    "l.receiverInteractor.id=:pageId AND l.deletedAt IS NULL)"
  )
  List<Person> findPersonsWhoLikedPage(@Param("pageId") Long pageId);
}
