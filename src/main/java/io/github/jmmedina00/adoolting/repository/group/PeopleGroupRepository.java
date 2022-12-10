package io.github.jmmedina00.adoolting.repository.group;

import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PeopleGroupRepository
  extends JpaRepository<PeopleGroup, Long> {
  @Query(
    "SELECT g FROM PeopleGroup g WHERE g.id=:groupId and g.deletedAt IS NULL"
  )
  Optional<PeopleGroup> findActiveGroup(@Param("groupId") Long groupId);
}
