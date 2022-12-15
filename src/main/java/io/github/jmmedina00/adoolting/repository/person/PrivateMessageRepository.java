package io.github.jmmedina00.adoolting.repository.person;

import io.github.jmmedina00.adoolting.entity.person.PrivateMessage;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrivateMessageRepository
  extends JpaRepository<PrivateMessage, Long> {
  @Query(
    "SELECT m FROM PrivateMessage m WHERE m.fromPerson.id=:personId OR " +
    "m.toPerson.id=:personId ORDER BY m.createdAt DESC"
  )
  List<PrivateMessage> findMessagesExchangedWithPerson(
    @Param("personId") Long personId
  );

  @Query(
    "SELECT m FROM PrivateMessage m WHERE (m.fromPerson.id=:firstPerson AND " +
    "m.toPerson.id=:secondPerson) OR (m.fromPerson.id=:secondPerson AND " +
    "m.toPerson.id=:firstPerson) ORDER BY m.createdAt DESC"
  )
  Page<PrivateMessage> findMessagesByPersonIds(
    @Param("firstPerson") Long firstPersonId,
    @Param("secondPerson") Long secondPersonId,
    Pageable pageable
  );
}
